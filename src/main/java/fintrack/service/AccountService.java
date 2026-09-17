package fintrack.service;

import fintrack.factory.AccountFactory;
import fintrack.model.Account;
import fintrack.model.AccountType;
import fintrack.observer.AlertEvent;
import fintrack.observer.AlertPublisher;
import fintrack.persistence.JsonFileStore;
import fintrack.util.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing Account creation, updates, deletions, and balance operations.
 * Leverages AccountFactory to instantiate polymorphic Account types.
 */
public class AccountService {

    private final JsonFileStore fileStore;
    private final AlertPublisher alertPublisher;

    public AccountService(JsonFileStore fileStore, AlertPublisher alertPublisher) {
        this.fileStore = fileStore;
        this.alertPublisher = alertPublisher;
    }

    /**
     * Creates an account using AccountFactory and saves it to persistence.
     */
    public synchronized Account createAccount(String userId, String accountName, AccountType type,
                                              double initialBalance, double param1, double param2) {
        Account account = AccountFactory.createAccount(type, userId, accountName, initialBalance, param1, param2);
        JsonFileStore.DataContainer data = fileStore.loadData();
        data.accounts.add(account);
        fileStore.saveData(data);

        Logger.getInstance().info(String.format("Created %s account '%s' (#%s) with initial balance ₹%,.2f",
                type, accountName, account.getAccountId(), initialBalance));

        alertPublisher.notifyObservers(new AlertEvent(
                AlertEvent.EventType.SYSTEM_INFO,
                "Account Created",
                String.format("%s account '%s' opened with initial balance ₹%,.2f", type.getDisplayName(), accountName, initialBalance),
                account
        ));

        return account;
    }

    public synchronized List<Account> getAccountsForUser(String userId) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        return data.accounts.stream()
                .filter(a -> a.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public synchronized Optional<Account> getAccountById(String accountId) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        return data.accounts.stream()
                .filter(a -> a.getAccountId().equalsIgnoreCase(accountId))
                .findFirst();
    }

    public synchronized boolean updateAccountName(String accountId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }
        JsonFileStore.DataContainer data = fileStore.loadData();
        for (Account a : data.accounts) {
            if (a.getAccountId().equalsIgnoreCase(accountId)) {
                a.setAccountName(newName.trim());
                fileStore.saveData(data);
                Logger.getInstance().info("Renamed account #" + accountId + " to '" + newName + "'");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteAccount(String accountId, String userId) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        boolean removed = data.accounts.removeIf(a -> a.getAccountId().equalsIgnoreCase(accountId) && a.getUserId().equals(userId));
        if (removed) {
            // Also remove transactions tied to this account
            data.transactions.removeIf(t -> t.getAccountId().equalsIgnoreCase(accountId) && t.getUserId().equals(userId));
            fileStore.saveData(data);
            Logger.getInstance().info("Deleted account #" + accountId + " and associated transactions.");
            return true;
        }
        return false;
    }

    public synchronized void updateAccount(Account account) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        for (int i = 0; i < data.accounts.size(); i++) {
            if (data.accounts.get(i).getAccountId().equals(account.getAccountId())) {
                data.accounts.set(i, account);
                fileStore.saveData(data);
                return;
            }
        }
    }
}
