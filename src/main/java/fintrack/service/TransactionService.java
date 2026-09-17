package fintrack.service;

import fintrack.model.*;
import fintrack.observer.AlertEvent;
import fintrack.observer.AlertPublisher;
import fintrack.persistence.JsonFileStore;
import fintrack.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing Transaction creation, deletion, categorization, and balance enforcement.
 * Coordinates real-time anomaly detection and budget limit evaluations.
 */
public class TransactionService {

    private final JsonFileStore fileStore;
    private final AccountService accountService;
    private final AnomalyService anomalyService;
    private final BudgetService budgetService;
    private final AlertPublisher alertPublisher;

    public TransactionService(JsonFileStore fileStore, AccountService accountService,
                              AnomalyService anomalyService, BudgetService budgetService,
                              AlertPublisher alertPublisher) {
        this.fileStore = fileStore;
        this.accountService = accountService;
        this.anomalyService = anomalyService;
        this.budgetService = budgetService;
        this.alertPublisher = alertPublisher;
    }

    /**
     * Records a new transaction, updating account balance polymorphically,
     * evaluating anomaly detection, and verifying budget thresholds.
     */
    public synchronized Transaction addTransaction(String userId, String accountId, TransactionType type,
                                                   Category category, double amount, String date, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly greater than zero.");
        }

        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        if (!account.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized: Account does not belong to the current user.");
        }

        // Apply polymorphic financial action
        if (type == TransactionType.EXPENSE) {
            boolean success = account.withdraw(amount);
            if (!success) {
                throw new IllegalStateException(String.format(
                        "Transaction rejected: Insufficient funds/limit in account '%s' (Available: ₹%,.2f).",
                        account.getAccountName(), account.getAvailableFunds()));
            }
        } else if (type == TransactionType.INCOME) {
            boolean success = account.deposit(amount);
            if (!success) {
                throw new IllegalStateException("Failed to deposit funds into account.");
            }
        }

        // Save updated account balance
        accountService.updateAccount(account);

        Transaction transaction = new Transaction(userId, accountId, type, category, amount, date, description);

        // Real-time Anomaly Detection on expense transactions
        if (type == TransactionType.EXPENSE) {
            List<Transaction> userHistory = getTransactionsForUser(userId);
            AnomalyResult result = anomalyService.evaluateTransaction(transaction, userHistory);

            if (result.isAnomaly()) {
                transaction.setFlaggedAsAnomaly(true);
                transaction.setAnomalyReason(result.getExplanation());

                Logger.getInstance().warn(String.format("Anomaly flagged for transaction #%s: %s",
                        transaction.getTransactionId(), result.getExplanation()));

                alertPublisher.notifyObservers(new AlertEvent(
                        AlertEvent.EventType.ANOMALY_DETECTED,
                        "Unusual Expense Flagged",
                        result.getExplanation(),
                        transaction
                ));
            }
        }

        // Save transaction to JSON store
        JsonFileStore.DataContainer data = fileStore.loadData();
        data.transactions.add(transaction);
        fileStore.saveData(data);

        Logger.getInstance().info(String.format("Added transaction #%s | %s | ₹%,.2f",
                transaction.getTransactionId(), type, amount));

        // Evaluate monthly budget thresholds
        if (type == TransactionType.EXPENSE) {
            List<Transaction> allUserTxns = getTransactionsForUser(userId);
            budgetService.checkBudgetThresholds(userId, category, date, allUserTxns);
        }

        return transaction;
    }

    /**
     * Executes a fund transfer between two accounts owned by the user.
     */
    public synchronized boolean transferFunds(String userId, String fromAccountId, String toAccountId,
                                              double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }
        if (fromAccountId.equalsIgnoreCase(toAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be identical.");
        }

        Account source = accountService.getAccountById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + fromAccountId));
        Account target = accountService.getAccountById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Target account not found: " + toAccountId));

        if (!source.getUserId().equals(userId) || !target.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized transfer: Accounts must belong to active user.");
        }

        if (!source.withdraw(amount)) {
            throw new IllegalStateException("Insufficient funds in source account for transfer.");
        }
        if (!target.deposit(amount)) {
            source.deposit(amount); // Rollback
            throw new IllegalStateException("Failed to deposit to target account.");
        }

        accountService.updateAccount(source);
        accountService.updateAccount(target);

        // Record transfer transactions
        Transaction transferTxn = new Transaction(userId, fromAccountId, TransactionType.TRANSFER,
                Category.OTHER, amount, null, description + " (to #" + target.getAccountId() + ")");
        transferTxn.setTargetAccountId(toAccountId);

        JsonFileStore.DataContainer data = fileStore.loadData();
        data.transactions.add(transferTxn);
        fileStore.saveData(data);

        Logger.getInstance().info(String.format("Transferred ₹%,.2f from #%s to #%s", amount, fromAccountId, toAccountId));
        return true;
    }

    public synchronized boolean deleteTransaction(String transactionId, String userId) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        Optional<Transaction> txnOpt = data.transactions.stream()
                .filter(t -> t.getTransactionId().equalsIgnoreCase(transactionId) && t.getUserId().equals(userId))
                .findFirst();

        if (txnOpt.isEmpty()) return false;

        Transaction txn = txnOpt.get();
        // Reverse account balance effect
        Optional<Account> accOpt = accountService.getAccountById(txn.getAccountId());
        if (accOpt.isPresent()) {
            Account acc = accOpt.get();
            if (txn.getType() == TransactionType.EXPENSE) {
                acc.deposit(txn.getAmount());
            } else if (txn.getType() == TransactionType.INCOME) {
                acc.withdraw(txn.getAmount());
            }
            accountService.updateAccount(acc);
        }

        data.transactions.remove(txn);
        fileStore.saveData(data);
        Logger.getInstance().info("Deleted transaction #" + transactionId);
        return true;
    }

    public synchronized List<Transaction> getTransactionsForUser(String userId) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        return data.transactions.stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public synchronized List<Transaction> getFlaggedAnomalies(String userId) {
        return getTransactionsForUser(userId).stream()
                .filter(Transaction::isFlaggedAsAnomaly)
                .collect(Collectors.toList());
    }
}
