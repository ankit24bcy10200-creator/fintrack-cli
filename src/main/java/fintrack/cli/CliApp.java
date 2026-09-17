package fintrack.cli;

import fintrack.observer.AlertPublisher;
import fintrack.observer.AnomalyObserver;
import fintrack.observer.AuditLogObserver;
import fintrack.observer.BudgetObserver;
import fintrack.persistence.JsonFileStore;
import fintrack.service.*;
import fintrack.util.Logger;

/**
 * Main application entry point for FinTrack CLI.
 * Wires the layered architecture, initializes Singleton Logger,
 * attaches Observer subscribers, and runs the top-level authentication loop.
 */
public class CliApp {

    public static void main(String[] args) {
        // Safe global exception guard
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Logger.getInstance().error("Uncaught exception in thread " + t.getName(), e);
            ConsoleUtil.printError("An unexpected error occurred: " + e.getMessage());
        });

        Logger logger = Logger.getInstance();
        logger.info("Initializing FinTrack CLI Application...");

        // 1. Initialize Persistence Layer
        JsonFileStore fileStore = new JsonFileStore();

        // 2. Initialize Observer Pattern (Subject + Subscribers)
        AlertPublisher alertPublisher = new AlertPublisher();
        BudgetObserver budgetObserver = new BudgetObserver();
        AnomalyObserver anomalyObserver = new AnomalyObserver();
        AuditLogObserver auditLogObserver = new AuditLogObserver();

        alertPublisher.attach(budgetObserver);
        alertPublisher.attach(anomalyObserver);
        alertPublisher.attach(auditLogObserver);
        logger.info("Attached 3 AlertObservers (Budget, Anomaly, AuditLog).");

        // 3. Initialize Services (Layered Architecture)
        AuthService authService = new AuthService(fileStore, alertPublisher);
        AccountService accountService = new AccountService(fileStore, alertPublisher);
        AnomalyService anomalyService = new AnomalyService();
        BudgetService budgetService = new BudgetService(fileStore, alertPublisher);
        TransactionService transactionService = new TransactionService(fileStore, accountService, anomalyService, budgetService, alertPublisher);
        ReportService reportService = new ReportService();

        // 4. Initialize CLI Menu Controller
        Menu menu = new Menu(authService, accountService, transactionService, budgetService, anomalyService, reportService);

        // 5. Run Authentication Loop
        ConsoleUtil.printBanner();
        boolean exitApp = false;

        while (!exitApp) {
            ConsoleUtil.printHeader("Welcome to FinTrack CLI");
            System.out.println("  1. Login to Existing Account");
            System.out.println("  2. Register New User");
            System.out.println("  3. Exit FinTrack");
            System.out.println();

            int choice = ConsoleUtil.readChoice("Select Option", 1, 3);
            switch (choice) {
                case 1:
                    loginPrompt(authService, menu, anomalyService);
                    break;
                case 2:
                    registerPrompt(authService);
                    break;
                case 3:
                    exitApp = true;
                    System.out.println("\nThank you for using FinTrack CLI. Have a secure financial day!\n");
                    logger.info("FinTrack CLI terminated gracefully.");
                    break;
            }
        }
    }

    private static void loginPrompt(AuthService authService, Menu menu, AnomalyService anomalyService) {
        ConsoleUtil.printHeader("User Login");
        String username = ConsoleUtil.readString("Username");
        String password = ConsoleUtil.readPassword("Password");

        if (authService.login(username, password)) {
            ConsoleUtil.printSuccess("Welcome back, " + authService.getCurrentUser().getFullName() + "!");

            // Synchronize user preferred strategy
            String prefStrategy = authService.getCurrentUser().getActiveStrategy();
            double prefThreshold = "Z_SCORE".equalsIgnoreCase(prefStrategy)
                    ? authService.getCurrentUser().getZScoreThreshold()
                    : authService.getCurrentUser().getIqrMultiplier();
            try {
                anomalyService.setActiveStrategy(prefStrategy, prefThreshold);
            } catch (Exception ignored) {
            }

            menu.showMainMenu();
        } else {
            ConsoleUtil.printError("Invalid username or password. Please try again.");
            ConsoleUtil.pause();
        }
    }

    private static void registerPrompt(AuthService authService) {
        ConsoleUtil.printHeader("User Registration");
        String fullName = ConsoleUtil.readString("Full Name");
        String username = ConsoleUtil.readString("Choose Username (alphanumeric, 3-20 chars)");
        String password = ConsoleUtil.readPassword("Choose Secure Password (min 6 characters)");

        try {
            boolean success = authService.register(username, password, fullName);
            if (success) {
                ConsoleUtil.printSuccess("Registration successful! You can now log in with your credentials.");
            } else {
                ConsoleUtil.printError("Username '" + username + "' is already taken. Please choose another.");
            }
        } catch (IllegalArgumentException e) {
            ConsoleUtil.printError("Registration failed: " + e.getMessage());
        }
        ConsoleUtil.pause();
    }
}
