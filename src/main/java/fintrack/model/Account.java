package fintrack.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base class representing a generic financial account.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class Account {
    protected String accountId;
    protected String userId;
    protected String accountName;
    protected AccountType accountType;
    protected double balance;
    protected String createdAt;

    public Account() {
        // Default constructor for serialization
    }

    public Account(String accountId, String userId, String accountName, AccountType accountType, double initialBalance) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty.");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be null or empty.");
        }
        this.accountId = accountId;
        this.userId = userId;
        this.accountName = accountName;
        this.accountType = accountType;
        this.balance = initialBalance;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Attempts to withdraw a specified amount from the account.
     * Concrete account classes implement polymorphic rules (overdraft limits, min balance, credit limits).
     *
     * @param amount the amount to withdraw
     * @return true if withdrawal succeeded, false otherwise
     */
    public abstract boolean withdraw(double amount);

    /**
     * Deposits money into the account or pays down outstanding credit.
     *
     * @param amount the amount to deposit
     * @return true if deposit succeeded, false otherwise
     */
    public abstract boolean deposit(double amount);

    /**
     * Polymorphic calculation of monthly interest earned (Savings) or finance charge (Credit).
     *
     * @return monthly interest amount
     */
    public abstract double calculateMonthlyInterest();

    /**
     * Returns the total usable funds available to the account holder.
     *
     * @return available spending power
     */
    public abstract double getAvailableFunds();

    /**
     * Provides a detailed account summary formatted for reports.
     *
     * @return formatted summary string
     */
    public abstract String getAccountSummary();

    // Getters and Setters with Encapsulation
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty.");
        }
        this.accountName = accountName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (#%s) | Balance: ₹%,.2f",
                accountType != null ? accountType.name() : "ACCOUNT",
                accountName,
                accountId,
                balance);
    }
}
