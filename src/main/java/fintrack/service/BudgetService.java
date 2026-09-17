package fintrack.service;

import fintrack.model.Budget;
import fintrack.model.Category;
import fintrack.model.Transaction;
import fintrack.model.TransactionType;
import fintrack.observer.AlertEvent;
import fintrack.observer.AlertPublisher;
import fintrack.persistence.JsonFileStore;
import fintrack.util.Logger;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing category budgets and monitoring threshold breaches.
 * Integrates with AlertPublisher to dispatch warning and breach events (Observer Pattern).
 */
public class BudgetService {

    private final JsonFileStore fileStore;
    private final AlertPublisher alertPublisher;

    public BudgetService(JsonFileStore fileStore, AlertPublisher alertPublisher) {
        this.fileStore = fileStore;
        this.alertPublisher = alertPublisher;
    }

    /**
     * Sets or updates a monthly budget limit for a category.
     */
    public synchronized Budget setBudget(String userId, Category category, double monthlyLimit, int month, int year) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        Optional<Budget> existing = data.budgets.stream()
                .filter(b -> b.getUserId().equals(userId) && b.getCategory() == category && b.getMonth() == month && b.getYear() == year)
                .findFirst();

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setMonthlyLimit(monthlyLimit);
            budget.setWarningAlertSent(false);
            budget.setBreachAlertSent(false);
            Logger.getInstance().info(String.format("Updated budget for %s (%d/%d): ₹%,.2f", category, month, year, monthlyLimit));
        } else {
            budget = new Budget(userId, category, monthlyLimit, month, year);
            data.budgets.add(budget);
            Logger.getInstance().info(String.format("Created budget for %s (%d/%d): ₹%,.2f", category, month, year, monthlyLimit));
        }

        fileStore.saveData(data);
        return budget;
    }

    public synchronized List<Budget> getBudgetsForUser(String userId, int month, int year) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        return data.budgets.stream()
                .filter(b -> b.getUserId().equals(userId) && b.getMonth() == month && b.getYear() == year)
                .collect(Collectors.toList());
    }

    public synchronized List<Budget> getAllBudgetsForUser(String userId) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        return data.budgets.stream()
                .filter(b -> b.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public synchronized boolean deleteBudget(String budgetId, String userId) {
        JsonFileStore.DataContainer data = fileStore.loadData();
        boolean removed = data.budgets.removeIf(b -> b.getBudgetId().equalsIgnoreCase(budgetId) && b.getUserId().equals(userId));
        if (removed) {
            fileStore.saveData(data);
            Logger.getInstance().info("Deleted budget #" + budgetId);
        }
        return removed;
    }

    /**
     * Checks if a new expense transaction triggers budget warning or breach alerts.
     */
    public synchronized void checkBudgetThresholds(String userId, Category category, String dateStr, List<Transaction> userTransactions) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            date = LocalDate.now();
        }

        int month = date.getMonthValue();
        int year = date.getYear();

        JsonFileStore.DataContainer data = fileStore.loadData();
        Optional<Budget> budgetOpt = data.budgets.stream()
                .filter(b -> b.getUserId().equals(userId) && b.getCategory() == category && b.getMonth() == month && b.getYear() == year)
                .findFirst();

        if (budgetOpt.isEmpty()) {
            return; // No budget defined for this category and month
        }

        Budget budget = budgetOpt.get();

        // Calculate total spending in this category for the month
        double totalSpent = userTransactions.stream()
                .filter(t -> t.getUserId().equals(userId))
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategory() == category)
                .filter(t -> {
                    try {
                        LocalDate td = LocalDate.parse(t.getDate());
                        return td.getMonthValue() == month && td.getYear() == year;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .mapToDouble(Transaction::getAmount)
                .sum();

        double limit = budget.getMonthlyLimit();
        double usageRatio = (limit > 0) ? (totalSpent / limit) : 0.0;

        if (usageRatio >= 1.0 && !budget.isBreachAlertSent()) {
            budget.setBreachAlertSent(true);
            budget.setWarningAlertSent(true);
            fileStore.saveData(data);

            alertPublisher.notifyObservers(new AlertEvent(
                    AlertEvent.EventType.BUDGET_BREACHED,
                    String.format("Budget Exceeded for %s", category.getDisplayName()),
                    String.format("Total spent ₹%,.2f exceeds limit ₹%,.2f (%.1f%% of budget)",
                            totalSpent, limit, usageRatio * 100.0),
                    budget
            ));
        } else if (usageRatio >= 0.80 && usageRatio < 1.0 && !budget.isWarningAlertSent()) {
            budget.setWarningAlertSent(true);
            fileStore.saveData(data);

            alertPublisher.notifyObservers(new AlertEvent(
                    AlertEvent.EventType.BUDGET_WARNING,
                    String.format("Budget Warning (80%%) for %s", category.getDisplayName()),
                    String.format("Total spent ₹%,.2f has reached %.1f%% of limit ₹%,.2f",
                            totalSpent, usageRatio * 100.0, limit),
                    budget
            ));
        }
    }

    /**
     * Calculates total spent in each category for a user in a given month/year.
     */
    public Map<String, Double> calculateCategorySpending(String userId, int month, int year, List<Transaction> transactions) {
        Map<String, Double> spending = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getUserId().equals(userId) && t.getType() == TransactionType.EXPENSE) {
                try {
                    LocalDate d = LocalDate.parse(t.getDate());
                    if (d.getMonthValue() == month && d.getYear() == year) {
                        String catName = t.getCategory().name();
                        spending.put(catName, spending.getOrDefault(catName, 0.0) + t.getAmount());
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return spending;
    }
}
