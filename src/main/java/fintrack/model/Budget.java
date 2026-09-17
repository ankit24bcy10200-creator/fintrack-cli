package fintrack.model;

import java.util.UUID;

/**
 * Model representing a monthly category budget limit.
 * Tracks threshold alert states for the Observer pattern.
 */
public class Budget {
    private String budgetId;
    private String userId;
    private Category category;
    private double monthlyLimit;
    private int month; // 1-12
    private int year;
    private boolean warningAlertSent;
    private boolean breachAlertSent;

    public Budget() {
    }

    public Budget(String userId, Category category, double monthlyLimit, int month, int year) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null.");
        }
        if (monthlyLimit <= 0) {
            throw new IllegalArgumentException("Monthly limit must be greater than zero.");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12.");
        }
        this.budgetId = "BGT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.userId = userId;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.month = month;
        this.year = year;
        this.warningAlertSent = false;
        this.breachAlertSent = false;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isWarningAlertSent() {
        return warningAlertSent;
    }

    public void setWarningAlertSent(boolean warningAlertSent) {
        this.warningAlertSent = warningAlertSent;
    }

    public boolean isBreachAlertSent() {
        return breachAlertSent;
    }

    public void setBreachAlertSent(boolean breachAlertSent) {
        this.breachAlertSent = breachAlertSent;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d/%d): Limit ₹%,.2f",
                budgetId, category.getDisplayName(), month, year, monthlyLimit);
    }
}
