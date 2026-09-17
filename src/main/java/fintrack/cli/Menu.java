package fintrack.cli;

import fintrack.model.*;
import fintrack.persistence.CsvExporter;
import fintrack.service.*;
import fintrack.strategy.IQRStrategy;
import fintrack.strategy.ZScoreStrategy;

import java.time.LocalDate;
import java.util.*;

/**
 * Controller for FinTrack CLI menus, user prompts, and visual workflows.
 */
public class Menu {

    private final AuthService authService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final AnomalyService anomalyService;
    private final ReportService reportService;

    public Menu(AuthService authService, AccountService accountService,
                TransactionService transactionService, BudgetService budgetService,
                AnomalyService anomalyService, ReportService reportService) {
        this.authService = authService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.anomalyService = anomalyService;
        this.reportService = reportService;
    }

    public void showMainMenu() {
        User user = authService.getCurrentUser();
        boolean running = true;

        while (running && authService.isLoggedIn()) {
            ConsoleUtil.printHeader("FinTrack Dashboard | User: " + user.getFullName() + " (@" + user.getUsername() + ")");
            System.out.println("  1. " + ConsoleUtil.CYAN + "Account Management" + ConsoleUtil.RESET + " (Savings, Checking, Credit)");
            System.out.println("  2. " + ConsoleUtil.GREEN + "Transaction Management" + ConsoleUtil.RESET + " (Expenses, Income, Transfers)");
            System.out.println("  3. " + ConsoleUtil.YELLOW + "Budget Management" + ConsoleUtil.RESET + " (Monthly Category Limits)");
            System.out.println("  4. " + ConsoleUtil.MAGENTA + "Statistical Anomaly Detection Engine" + ConsoleUtil.RESET + " (Z-Score / IQR)");
            System.out.println("  5. " + ConsoleUtil.BLUE + "Spending Analytics & Reports" + ConsoleUtil.RESET + " (Trends, Charts, CSV Export)");
            System.out.println("  6. " + ConsoleUtil.WHITE + "Configure Anomaly Strategy & Settings" + ConsoleUtil.RESET);
            System.out.println("  7. " + ConsoleUtil.BOLD + "⚡ Seed Realistic Demo Financial Data" + ConsoleUtil.RESET);
            System.out.println("  8. Logout");
            System.out.println();

            int choice = ConsoleUtil.readChoice("Select Option", 1, 8);
            switch (choice) {
                case 1: showAccountMenu(); break;
                case 2: showTransactionMenu(); break;
                case 3: showBudgetMenu(); break;
                case 4: showAnomalyMenu(); break;
                case 5: showReportMenu(); break;
                case 6: showSettingsMenu(); break;
                case 7: seedDemoData(); break;
                case 8:
                    authService.logout();
                    ConsoleUtil.printSuccess("Logged out safely. Goodbye!");
                    running = false;
                    break;
            }
        }
    }

    // ==========================================
    // MODULE 1: ACCOUNT MANAGEMENT
    // ==========================================
    private void showAccountMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printHeader("Account Management");
            System.out.println("  1. List My Accounts");
            System.out.println("  2. Open New Account (Savings / Checking / Credit)");
            System.out.println("  3. Rename Account");
            System.out.println("  4. Delete Account");
            System.out.println("  5. Return to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.readChoice("Select Option", 1, 5);
            switch (choice) {
                case 1: listAccountsView(); ConsoleUtil.pause(); break;
                case 2: createAccountPrompt(); ConsoleUtil.pause(); break;
                case 3: renameAccountPrompt(); ConsoleUtil.pause(); break;
                case 4: deleteAccountPrompt(); ConsoleUtil.pause(); break;
                case 5: back = true; break;
            }
        }
    }

    private void listAccountsView() {
        String userId = authService.getCurrentUser().getUserId();
        List<Account> accounts = accountService.getAccountsForUser(userId);

        String[] headers = {"Account ID", "Name", "Type", "Balance (₹)", "Available Funds (₹)", "Details / Limits"};
        List<String[]> rows = new ArrayList<>();

        double totalNetWorth = 0.0;
        for (Account a : accounts) {
            String details = "";
            if (a instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) a;
                details = String.format("Min Bal: ₹%,.0f | Int: %.1f%%", sa.getMinimumBalance(), sa.getInterestRate());
                totalNetWorth += a.getBalance();
            } else if (a instanceof CheckingAccount) {
                CheckingAccount ca = (CheckingAccount) a;
                details = String.format("Overdraft: ₹%,.0f | Fee: ₹%.0f", ca.getOverdraftLimit(), ca.getTransactionFee());
                totalNetWorth += a.getBalance();
            } else if (a instanceof CreditAccount) {
                CreditAccount ca = (CreditAccount) a;
                details = String.format("Credit Limit: ₹%,.0f | APR: %.1f%% (Debt: ₹%,.2f)", ca.getCreditLimit(), ca.getAnnualPercentageRate(), ca.getBalance());
                totalNetWorth -= a.getBalance(); // Outstanding debt reduces net worth
            }

            rows.add(new String[]{
                    a.getAccountId(),
                    a.getAccountName(),
                    a.getAccountType().name(),
                    String.format("₹%,.2f", a.getBalance()),
                    String.format("₹%,.2f", a.getAvailableFunds()),
                    details
            });
        }

        ConsoleUtil.renderTable(headers, rows);
        System.out.printf("\nEstimated Net Portfolio Worth: %s₹%,.2f%s\n",
                ConsoleUtil.BOLD + ConsoleUtil.GREEN, totalNetWorth, ConsoleUtil.RESET);
    }

    private void createAccountPrompt() {
        String userId = authService.getCurrentUser().getUserId();
        ConsoleUtil.printHeader("Open New Account");
        System.out.println("Select Account Type:");
        System.out.println("  1. Savings Account (Interest earning, minimum balance)");
        System.out.println("  2. Checking Account (Daily expenses, overdraft protection)");
        System.out.println("  3. Credit Account (Revolving credit card line)");

        int typeChoice = ConsoleUtil.readChoice("Choose Account Type", 1, 3);
        String name = ConsoleUtil.readString("Enter Account Label/Name (e.g. HDFC Salary, SBI Emergency)");

        try {
            if (typeChoice == 1) {
                double initBal = ConsoleUtil.readPositiveDouble("Initial Deposit Amount (₹)");
                double minBal = ConsoleUtil.readNonNegativeDouble("Minimum Balance Requirement (₹, e.g. 1000)");
                double intRate = ConsoleUtil.readNonNegativeDouble("Annual Interest Rate (%, e.g. 4.0)");
                accountService.createAccount(userId, name, AccountType.SAVINGS, initBal, intRate, minBal);
            } else if (typeChoice == 2) {
                double initBal = ConsoleUtil.readPositiveDouble("Initial Deposit Amount (₹)");
                double overdraft = ConsoleUtil.readNonNegativeDouble("Overdraft Limit (₹, e.g. 5000)");
                double fee = ConsoleUtil.readNonNegativeDouble("Transaction Fee for Overdraft (₹, e.g. 10)");
                accountService.createAccount(userId, name, AccountType.CHECKING, initBal, overdraft, fee);
            } else {
                double creditLimit = ConsoleUtil.readPositiveDouble("Credit Limit (₹, e.g. 50000)");
                double apr = ConsoleUtil.readNonNegativeDouble("Annual Percentage Rate (%, e.g. 18.0)");
                accountService.createAccount(userId, name, AccountType.CREDIT, 0.0, creditLimit, apr);
            }
            ConsoleUtil.printSuccess("Account successfully created via AccountFactory!");
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to create account: " + e.getMessage());
        }
    }

    private void renameAccountPrompt() {
        listAccountsView();
        String accId = ConsoleUtil.readString("Enter Account ID to rename");
        String newName = ConsoleUtil.readString("Enter new account name");
        if (accountService.updateAccountName(accId, newName)) {
            ConsoleUtil.printSuccess("Account updated successfully.");
        } else {
            ConsoleUtil.printError("Account not found.");
        }
    }

    private void deleteAccountPrompt() {
        listAccountsView();
        String accId = ConsoleUtil.readString("Enter Account ID to delete");
        String confirm = ConsoleUtil.readString("Are you sure? This will delete all associated transactions (type 'YES' to confirm)");
        if ("YES".equalsIgnoreCase(confirm)) {
            if (accountService.deleteAccount(accId, authService.getCurrentUser().getUserId())) {
                ConsoleUtil.printSuccess("Account and tied transactions deleted.");
            } else {
                ConsoleUtil.printError("Account not found or access denied.");
            }
        } else {
            ConsoleUtil.printInfo("Deletion cancelled.");
        }
    }

    // ==========================================
    // MODULE 2: TRANSACTION & BUDGET MANAGEMENT
    // ==========================================
    private void showTransactionMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printHeader("Transaction Management");
            System.out.println("  1. Record Expense");
            System.out.println("  2. Record Income");
            System.out.println("  3. Transfer Funds Between Accounts");
            System.out.println("  4. View All Transactions");
            System.out.println("  5. Filter Transactions by Category");
            System.out.println("  6. Delete a Transaction");
            System.out.println("  7. Return to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.readChoice("Select Option", 1, 7);
            switch (choice) {
                case 1: addTransactionPrompt(TransactionType.EXPENSE); ConsoleUtil.pause(); break;
                case 2: addTransactionPrompt(TransactionType.INCOME); ConsoleUtil.pause(); break;
                case 3: transferFundsPrompt(); ConsoleUtil.pause(); break;
                case 4: listTransactionsView(transactionService.getTransactionsForUser(authService.getCurrentUser().getUserId())); ConsoleUtil.pause(); break;
                case 5: filterCategoryPrompt(); ConsoleUtil.pause(); break;
                case 6: deleteTransactionPrompt(); ConsoleUtil.pause(); break;
                case 7: back = true; break;
            }
        }
    }

    private void addTransactionPrompt(TransactionType type) {
        String userId = authService.getCurrentUser().getUserId();
        List<Account> accounts = accountService.getAccountsForUser(userId);
        if (accounts.isEmpty()) {
            ConsoleUtil.printWarning("Please open at least one bank/credit account first!");
            return;
        }

        ConsoleUtil.printHeader("Record " + type.getDisplayName());
        listAccountsView();
        String accId = ConsoleUtil.readString("Enter Account ID to use");

        System.out.println("\nSelect Category:");
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("  %2d. %s\n", (i + 1), categories[i].getDisplayName());
        }
        int catIdx = ConsoleUtil.readChoice("Choose Category", 1, categories.length);
        Category category = categories[catIdx - 1];

        double amount = ConsoleUtil.readPositiveDouble("Enter Amount (₹)");
        String date = ConsoleUtil.readDate("Transaction Date (YYYY-MM-DD)");
        String desc = ConsoleUtil.readString("Description / Payee");

        try {
            Transaction txn = transactionService.addTransaction(userId, accId, type, category, amount, date, desc);
            ConsoleUtil.printSuccess(String.format("Transaction recorded: %s | ₹%,.2f", txn.getTransactionId(), txn.getAmount()));
        } catch (Exception e) {
            ConsoleUtil.printError("Transaction failed: " + e.getMessage());
        }
    }

    private void transferFundsPrompt() {
        String userId = authService.getCurrentUser().getUserId();
        List<Account> accounts = accountService.getAccountsForUser(userId);
        if (accounts.size() < 2) {
            ConsoleUtil.printWarning("You need at least two accounts to execute a transfer.");
            return;
        }

        ConsoleUtil.printHeader("Transfer Funds");
        listAccountsView();
        String fromId = ConsoleUtil.readString("Source Account ID (Transfer From)");
        String toId = ConsoleUtil.readString("Destination Account ID (Transfer To)");
        double amount = ConsoleUtil.readPositiveDouble("Transfer Amount (₹)");
        String desc = ConsoleUtil.readOptionalString("Transfer Note", "Internal transfer");

        try {
            transactionService.transferFunds(userId, fromId, toId, amount, desc);
            ConsoleUtil.printSuccess("Transfer completed successfully!");
        } catch (Exception e) {
            ConsoleUtil.printError("Transfer failed: " + e.getMessage());
        }
    }

    private void listTransactionsView(List<Transaction> transactions) {
        String[] headers = {"Txn ID", "Date", "Account", "Type", "Category", "Amount (₹)", "Description", "Status"};
        List<String[]> rows = new ArrayList<>();

        for (Transaction t : transactions) {
            String typeColor = (t.getType() == TransactionType.EXPENSE) ? ConsoleUtil.RED :
                    (t.getType() == TransactionType.INCOME ? ConsoleUtil.GREEN : ConsoleUtil.BLUE);
            String status = t.isFlaggedAsAnomaly()
                    ? ConsoleUtil.MAGENTA + "⚠ ANOMALY" + ConsoleUtil.RESET
                    : "Normal";

            rows.add(new String[]{
                    t.getTransactionId(),
                    t.getDate(),
                    t.getAccountId(),
                    typeColor + t.getType().name() + ConsoleUtil.RESET,
                    t.getCategory().name(),
                    String.format("₹%,.2f", t.getAmount()),
                    t.getDescription(),
                    status
            });
        }

        ConsoleUtil.renderTable(headers, rows);
    }

    private void filterCategoryPrompt() {
        System.out.println("Select Category to filter:");
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("  %2d. %s\n", (i + 1), categories[i].getDisplayName());
        }
        int catIdx = ConsoleUtil.readChoice("Choose Category", 1, categories.length);
        Category selected = categories[catIdx - 1];

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactionService.getTransactionsForUser(authService.getCurrentUser().getUserId())) {
            if (t.getCategory() == selected) {
                filtered.add(t);
            }
        }

        ConsoleUtil.printHeader("Transactions in Category: " + selected.getDisplayName());
        listTransactionsView(filtered);
    }

    private void deleteTransactionPrompt() {
        listTransactionsView(transactionService.getTransactionsForUser(authService.getCurrentUser().getUserId()));
        String txnId = ConsoleUtil.readString("Enter Transaction ID to delete");
        if (transactionService.deleteTransaction(txnId, authService.getCurrentUser().getUserId())) {
            ConsoleUtil.printSuccess("Transaction removed and account balance restored.");
        } else {
            ConsoleUtil.printError("Transaction not found or access denied.");
        }
    }

    // ==========================================
    // MODULE 2 CONT.: BUDGET MANAGEMENT
    // ==========================================
    private void showBudgetMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printHeader("Monthly Budget Management");
            System.out.println("  1. View Current Budgets & Spending Status");
            System.out.println("  2. Set or Update Monthly Category Budget");
            System.out.println("  3. Delete a Budget");
            System.out.println("  4. Return to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.readChoice("Select Option", 1, 4);
            switch (choice) {
                case 1: listBudgetsView(); ConsoleUtil.pause(); break;
                case 2: setBudgetPrompt(); ConsoleUtil.pause(); break;
                case 3: deleteBudgetPrompt(); ConsoleUtil.pause(); break;
                case 4: back = true; break;
            }
        }
    }

    private void listBudgetsView() {
        String userId = authService.getCurrentUser().getUserId();
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        List<Budget> budgets = budgetService.getBudgetsForUser(userId, month, year);
        List<Transaction> txns = transactionService.getTransactionsForUser(userId);
        Map<String, Double> spending = budgetService.calculateCategorySpending(userId, month, year, txns);

        String[] headers = {"Budget ID", "Category", "Period", "Limit (₹)", "Spent (₹)", "Remaining (₹)", "Usage", "Status"};
        List<String[]> rows = new ArrayList<>();

        for (Budget b : budgets) {
            double spent = spending.getOrDefault(b.getCategory().name(), 0.0);
            double remaining = Math.max(0.0, b.getMonthlyLimit() - spent);
            double usagePct = (b.getMonthlyLimit() > 0) ? (spent / b.getMonthlyLimit()) * 100.0 : 0.0;

            String status;
            if (spent > b.getMonthlyLimit()) {
                status = ConsoleUtil.RED + "BREACHED" + ConsoleUtil.RESET;
            } else if (usagePct >= 80.0) {
                status = ConsoleUtil.YELLOW + "WARNING (>=80%)" + ConsoleUtil.RESET;
            } else {
                status = ConsoleUtil.GREEN + "HEALTHY" + ConsoleUtil.RESET;
            }

            rows.add(new String[]{
                    b.getBudgetId(),
                    b.getCategory().getDisplayName(),
                    String.format("%02d/%d", b.getMonth(), b.getYear()),
                    String.format("₹%,.2f", b.getMonthlyLimit()),
                    String.format("₹%,.2f", spent),
                    String.format("₹%,.2f", remaining),
                    ReportService.renderProgressBar(usagePct, 12),
                    status
            });
        }

        ConsoleUtil.printHeader(String.format("Budget Utilization for %s %d", now.getMonth().name(), year));
        ConsoleUtil.renderTable(headers, rows);
    }

    private void setBudgetPrompt() {
        String userId = authService.getCurrentUser().getUserId();
        LocalDate now = LocalDate.now();

        System.out.println("Select Category for Monthly Budget:");
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("  %2d. %s\n", (i + 1), categories[i].getDisplayName());
        }
        int catIdx = ConsoleUtil.readChoice("Choose Category", 1, categories.length);
        Category category = categories[catIdx - 1];

        double limit = ConsoleUtil.readPositiveDouble("Monthly Spending Limit (₹)");
        int month = ConsoleUtil.readChoice("Month (1-12)", 1, 12);
        int year = (int) ConsoleUtil.readPositiveDouble("Year (e.g. " + now.getYear() + ")");

        budgetService.setBudget(userId, category, limit, month, year);
        ConsoleUtil.printSuccess("Budget configured successfully for " + category.getDisplayName() + "!");
    }

    private void deleteBudgetPrompt() {
        listBudgetsView();
        String bgtId = ConsoleUtil.readString("Enter Budget ID to remove");
        if (budgetService.deleteBudget(bgtId, authService.getCurrentUser().getUserId())) {
            ConsoleUtil.printSuccess("Budget removed successfully.");
        } else {
            ConsoleUtil.printError("Budget not found.");
        }
    }

    // ==========================================
    // MODULE 3: ANOMALY DETECTION ENGINE
    // ==========================================
    private void showAnomalyMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printHeader("Statistical Anomaly Detection Engine");
            System.out.println("Active Algorithm: " + ConsoleUtil.MAGENTA + ConsoleUtil.BOLD +
                    anomalyService.getActiveStrategy().getStrategyName() + ConsoleUtil.RESET +
                    " (Sensitivity: " + anomalyService.getCurrentSensitivity() + ")");
            System.out.println("Description: " + anomalyService.getActiveStrategy().getDescription());
            System.out.println();
            System.out.println("  1. View Flagged Anomaly Transactions");
            System.out.println("  2. Run Full Historical Anomaly Scan");
            System.out.println("  3. Simulate What-If Expense Anomaly");
            System.out.println("  4. Switch Strategy (Z-Score <-> IQR)");
            System.out.println("  5. Return to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.readChoice("Select Option", 1, 5);
            switch (choice) {
                case 1: viewFlaggedAnomaliesView(); ConsoleUtil.pause(); break;
                case 2: runHistoricalScan(); ConsoleUtil.pause(); break;
                case 3: simulateWhatIfPrompt(); ConsoleUtil.pause(); break;
                case 4: showSettingsMenu(); break;
                case 5: back = true; break;
            }
        }
    }

    private void viewFlaggedAnomaliesView() {
        String userId = authService.getCurrentUser().getUserId();
        List<Transaction> anomalies = transactionService.getFlaggedAnomalies(userId);

        ConsoleUtil.printHeader("Flagged Anomalous Transactions (" + anomalies.size() + " Found)");
        if (anomalies.isEmpty()) {
            ConsoleUtil.printSuccess("No anomalies detected in your spending history!");
            return;
        }

        String[] headers = {"Txn ID", "Date", "Category", "Amount (₹)", "Description", "Statistical Reason"};
        List<String[]> rows = new ArrayList<>();

        for (Transaction t : anomalies) {
            rows.add(new String[]{
                    t.getTransactionId(),
                    t.getDate(),
                    t.getCategory().name(),
                    String.format("₹%,.2f", t.getAmount()),
                    t.getDescription(),
                    ConsoleUtil.RED + t.getAnomalyReason() + ConsoleUtil.RESET
            });
        }

        ConsoleUtil.renderTable(headers, rows);
    }

    private void runHistoricalScan() {
        String userId = authService.getCurrentUser().getUserId();
        List<Transaction> transactions = transactionService.getTransactionsForUser(userId);

        ConsoleUtil.printHeader("Executing Historical Anomaly Scan...");
        List<AnomalyResult> results = anomalyService.detectAll(transactions);

        int flaggedCount = 0;
        String[] headers = {"Txn ID", "Date", "Category", "Amount (₹)", "Score", "Result", "Explanation"};
        List<String[]> rows = new ArrayList<>();

        for (AnomalyResult res : results) {
            Transaction t = res.getTransaction();
            if (res.isAnomaly()) {
                flaggedCount++;
                t.setFlaggedAsAnomaly(true);
                t.setAnomalyReason(res.getExplanation());
            }

            rows.add(new String[]{
                    t.getTransactionId(),
                    t.getDate(),
                    t.getCategory().name(),
                    String.format("₹%,.2f", t.getAmount()),
                    String.format("%.2f", res.getScore()),
                    res.isAnomaly() ? ConsoleUtil.RED + "FLAGGED" + ConsoleUtil.RESET : ConsoleUtil.GREEN + "NORMAL" + ConsoleUtil.RESET,
                    res.getExplanation()
            });
        }

        ConsoleUtil.renderTable(headers, rows);
        System.out.printf("\nScan Complete: Evaluated %d transactions. %s%d anomalies flagged.%s\n",
                results.size(), ConsoleUtil.BOLD + ConsoleUtil.MAGENTA, flaggedCount, ConsoleUtil.RESET);
    }

    private void simulateWhatIfPrompt() {
        String userId = authService.getCurrentUser().getUserId();
        List<Transaction> history = transactionService.getTransactionsForUser(userId);

        ConsoleUtil.printHeader("Simulate 'What-If' Expense Anomaly Test");
        System.out.println("Select Category to test:");
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("  %2d. %s\n", (i + 1), categories[i].getDisplayName());
        }
        int catIdx = ConsoleUtil.readChoice("Choose Category", 1, categories.length);
        Category cat = categories[catIdx - 1];

        double amount = ConsoleUtil.readPositiveDouble("Hypothetical Expense Amount (₹)");
        Transaction dummy = new Transaction(userId, "SIM", TransactionType.EXPENSE, cat, amount, LocalDate.now().toString(), "What-If Simulation");

        AnomalyResult res = anomalyService.evaluateTransaction(dummy, history);

        System.out.println("\n--- SIMULATION RESULTS ---");
        System.out.println("Active Strategy : " + res.getStrategyName());
        System.out.println("Tested Amount   : ₹" + String.format("%,.2f", amount));
        System.out.println("Anomaly Flag    : " + (res.isAnomaly() ? ConsoleUtil.RED + "YES (ANOMALY)" + ConsoleUtil.RESET : ConsoleUtil.GREEN + "NO (NORMAL)" + ConsoleUtil.RESET));
        System.out.println("Score Computed  : " + String.format("%.2f (Threshold: %.2f)", res.getScore(), res.getThreshold()));
        System.out.println("Explanation     : " + res.getExplanation());
    }

    // ==========================================
    // MODULE 4: REPORTS & ANALYTICS
    // ==========================================
    private void showReportMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printHeader("Spending Analytics & Reports");
            System.out.println("  1. Spending Breakdown by Category");
            System.out.println("  2. Monthly Cash Flow Trends (Income vs Expense)");
            System.out.println("  3. Financial Health Executive Summary");
            System.out.println("  4. Export Transactions to CSV");
            System.out.println("  5. Export Budget Report to CSV");
            System.out.println("  6. Return to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.readChoice("Select Option", 1, 6);
            switch (choice) {
                case 1: showCategoryBreakdown(); ConsoleUtil.pause(); break;
                case 2: showMonthlyTrends(); ConsoleUtil.pause(); break;
                case 3: showExecutiveSummary(); ConsoleUtil.pause(); break;
                case 4: exportTransactionsCsv(); ConsoleUtil.pause(); break;
                case 5: exportBudgetCsv(); ConsoleUtil.pause(); break;
                case 6: back = true; break;
            }
        }
    }

    private void showCategoryBreakdown() {
        String userId = authService.getCurrentUser().getUserId();
        List<Transaction> txns = transactionService.getTransactionsForUser(userId);
        List<ReportService.CategoryReportItem> report = reportService.getSpendingByCategory(txns);

        ConsoleUtil.printHeader("Spending Breakdown by Category");
        String[] headers = {"Category", "Total Spent (₹)", "% Share", "Txn Count", "Visual Distribution"};
        List<String[]> rows = new ArrayList<>();

        double grandTotal = 0.0;
        for (ReportService.CategoryReportItem item : report) {
            grandTotal += item.totalAmount;
            rows.add(new String[]{
                    item.category.getDisplayName(),
                    String.format("₹%,.2f", item.totalAmount),
                    String.format("%.1f%%", item.percentage),
                    String.valueOf(item.transactionCount),
                    ReportService.renderProgressBar(item.percentage, 15)
            });
        }

        ConsoleUtil.renderTable(headers, rows);
        System.out.printf("\nTotal Lifetime Expenses: %s₹%,.2f%s\n", ConsoleUtil.BOLD + ConsoleUtil.RED, grandTotal, ConsoleUtil.RESET);
    }

    private void showMonthlyTrends() {
        String userId = authService.getCurrentUser().getUserId();
        List<Transaction> txns = transactionService.getTransactionsForUser(userId);
        List<ReportService.MonthlyTrendItem> trends = reportService.getMonthlyTrends(txns);

        ConsoleUtil.printHeader("Monthly Cash Flow & Savings Rate Trends");
        String[] headers = {"Month", "Total Inflow (₹)", "Total Outflow (₹)", "Net Savings (₹)", "Savings Rate"};
        List<String[]> rows = new ArrayList<>();

        for (ReportService.MonthlyTrendItem item : trends) {
            String netColor = item.netSavings >= 0 ? ConsoleUtil.GREEN : ConsoleUtil.RED;
            rows.add(new String[]{
                    item.monthYear,
                    String.format("₹%,.2f", item.totalIncome),
                    String.format("₹%,.2f", item.totalExpense),
                    netColor + String.format("₹%,.2f", item.netSavings) + ConsoleUtil.RESET,
                    String.format("%.1f%%", item.savingsRate)
            });
        }

        ConsoleUtil.renderTable(headers, rows);
    }

    private void showExecutiveSummary() {
        String userId = authService.getCurrentUser().getUserId();
        List<Account> accounts = accountService.getAccountsForUser(userId);
        List<Transaction> txns = transactionService.getTransactionsForUser(userId);

        double totalInflow = txns.stream().filter(t -> t.getType() == TransactionType.INCOME).mapToDouble(Transaction::getAmount).sum();
        double totalOutflow = txns.stream().filter(t -> t.getType() == TransactionType.EXPENSE).mapToDouble(Transaction::getAmount).sum();
        long anomalyCount = txns.stream().filter(Transaction::isFlaggedAsAnomaly).count();

        ConsoleUtil.printHeader("Financial Health Executive Summary");
        System.out.printf("  • Total Accounts Linked        : %d accounts\n", accounts.size());
        System.out.printf("  • Total Lifetime Inflow        : %s₹%,.2f%s\n", ConsoleUtil.GREEN, totalInflow, ConsoleUtil.RESET);
        System.out.printf("  • Total Lifetime Outflow       : %s₹%,.2f%s\n", ConsoleUtil.RED, totalOutflow, ConsoleUtil.RESET);
        System.out.printf("  • Cumulative Net Savings       : %s₹%,.2f%s\n",
                (totalInflow - totalOutflow >= 0 ? ConsoleUtil.GREEN : ConsoleUtil.RED),
                (totalInflow - totalOutflow), ConsoleUtil.RESET);
        System.out.printf("  • Statistical Anomalies Flagged: %s%d transactions%s\n",
                (anomalyCount > 0 ? ConsoleUtil.MAGENTA + ConsoleUtil.BOLD : ConsoleUtil.WHITE),
                anomalyCount, ConsoleUtil.RESET);
    }

    private void exportTransactionsCsv() {
        String userId = authService.getCurrentUser().getUserId();
        List<Transaction> txns = transactionService.getTransactionsForUser(userId);
        String path = CsvExporter.exportTransactions(txns, authService.getCurrentUser().getUsername());
        if (path != null) {
            ConsoleUtil.printSuccess("Transactions exported to: " + path);
        } else {
            ConsoleUtil.printError("Export failed.");
        }
    }

    private void exportBudgetCsv() {
        String userId = authService.getCurrentUser().getUserId();
        LocalDate now = LocalDate.now();
        List<Budget> budgets = budgetService.getAllBudgetsForUser(userId);
        List<Transaction> txns = transactionService.getTransactionsForUser(userId);
        Map<String, Double> spending = budgetService.calculateCategorySpending(userId, now.getMonthValue(), now.getYear(), txns);

        String path = CsvExporter.exportBudgetReport(budgets, spending, authService.getCurrentUser().getUsername());
        if (path != null) {
            ConsoleUtil.printSuccess("Budget report exported to: " + path);
        } else {
            ConsoleUtil.printError("Export failed.");
        }
    }

    // ==========================================
    // SETTINGS & DEMO SEEDER
    // ==========================================
    private void showSettingsMenu() {
        ConsoleUtil.printHeader("Anomaly Detection Strategy Settings");
        System.out.println("  1. Use Z-Score Strategy (Mean & Standard Deviation based)");
        System.out.println("  2. Use IQR Strategy (Interquartile Range & Tukey Fences)");

        int choice = ConsoleUtil.readChoice("Select Algorithm", 1, 2);
        if (choice == 1) {
            double threshold = ConsoleUtil.readPositiveDouble("Enter Z-Score Threshold (default 2.5, typical 1.5 to 3.5)");
            anomalyService.setActiveStrategy(ZScoreStrategy.NAME, threshold);
            authService.updateUserPreferences(ZScoreStrategy.NAME, threshold);
            ConsoleUtil.printSuccess("Active strategy updated to Z-Score (Threshold: " + threshold + "σ)");
        } else {
            double multiplier = ConsoleUtil.readPositiveDouble("Enter IQR Multiplier (default 1.5 for outliers, 3.0 for extreme)");
            anomalyService.setActiveStrategy(IQRStrategy.NAME, multiplier);
            authService.updateUserPreferences(IQRStrategy.NAME, multiplier);
            ConsoleUtil.printSuccess("Active strategy updated to IQR (Multiplier: " + multiplier + "x)");
        }
        ConsoleUtil.pause();
    }

    private void seedDemoData() {
        String userId = authService.getCurrentUser().getUserId();
        ConsoleUtil.printHeader("Seeding Realistic Financial Demo Dataset...");

        // 1. Create accounts
        Account savings = accountService.createAccount(userId, "SBI Wealth Savings", AccountType.SAVINGS, 50000.0, 4.5, 2000.0);
        Account checking = accountService.createAccount(userId, "HDFC Salary Checking", AccountType.CHECKING, 25000.0, 10000.0, 15.0);
        Account credit = accountService.createAccount(userId, "ICICI Platinum Credit Card", AccountType.CREDIT, 0.0, 100000.0, 18.5);

        // 2. Set monthly budgets
        int m = LocalDate.now().getMonthValue();
        int y = LocalDate.now().getYear();
        budgetService.setBudget(userId, Category.FOOD, 8000.0, m, y);
        budgetService.setBudget(userId, Category.UTILITIES, 4000.0, m, y);
        budgetService.setBudget(userId, Category.ENTERTAINMENT, 5000.0, m, y);
        budgetService.setBudget(userId, Category.SHOPPING, 10000.0, m, y);

        // 3. Populate income
        transactionService.addTransaction(userId, checking.getAccountId(), TransactionType.INCOME,
                Category.SALARY, 85000.0, LocalDate.now().minusDays(20).toString(), "Monthly Corporate Salary");

        // 4. Typical normal expenses
        double[] foodExpenses = {450.0, 620.0, 380.0, 750.0, 520.0, 680.0, 490.0, 810.0, 560.0};
        for (int i = 0; i < foodExpenses.length; i++) {
            transactionService.addTransaction(userId, checking.getAccountId(), TransactionType.EXPENSE,
                    Category.FOOD, foodExpenses[i], LocalDate.now().minusDays(18 - i).toString(), "Groceries & Cafe #" + (i + 1));
        }

        double[] utilExpenses = {1200.0, 1450.0, 1100.0, 950.0};
        for (int i = 0; i < utilExpenses.length; i++) {
            transactionService.addTransaction(userId, checking.getAccountId(), TransactionType.EXPENSE,
                    Category.UTILITIES, utilExpenses[i], LocalDate.now().minusDays(15 - (i * 2)).toString(), "Utility Bill #" + (i + 1));
        }

        double[] entExpenses = {800.0, 1200.0, 950.0, 1100.0};
        for (int i = 0; i < entExpenses.length; i++) {
            transactionService.addTransaction(userId, credit.getAccountId(), TransactionType.EXPENSE,
                    Category.ENTERTAINMENT, entExpenses[i], LocalDate.now().minusDays(12 - (i * 2)).toString(), "Movies & Streaming #" + (i + 1));
        }

        // 5. Inject Statistical Outliers / Anomalies!
        // Anomaly 1: Sudden massive Food expense (e.g. ₹18,500 banquet dinner when baseline is ₹500)
        ConsoleUtil.printInfo("Injecting Anomaly 1: Sudden ₹18,500 Banquet Dinner (normal Food baseline ₹400-₹800)...");
        transactionService.addTransaction(userId, credit.getAccountId(), TransactionType.EXPENSE,
                Category.FOOD, 18500.0, LocalDate.now().minusDays(3).toString(), "Luxury Five-Star Banquet Dinner [Outlier]");

        // Anomaly 2: Extreme Shopping transaction (e.g. ₹55,000 Apple Device)
        ConsoleUtil.printInfo("Injecting Anomaly 2: Extreme ₹55,000 Electronics Purchase...");
        transactionService.addTransaction(userId, credit.getAccountId(), TransactionType.EXPENSE,
                Category.SHOPPING, 55000.0, LocalDate.now().minusDays(1).toString(), "Flagship Smartphone Purchase [Outlier]");

        ConsoleUtil.printSuccess("Realistic demo financial dataset seeded successfully with 20+ transactions and 2 anomalies!");
        ConsoleUtil.pause();
    }
}
