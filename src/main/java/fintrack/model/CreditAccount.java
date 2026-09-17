package fintrack.model;

/**
 * Credit Account specialization.
 * Demonstrates Inheritance and Polymorphism.
 * In a credit account, 'balance' tracks current outstanding debt.
 * Withdrawals consume available credit; deposits repay outstanding debt.
 */
public class CreditAccount extends Account {
    private double creditLimit;             // Total credit line (e.g., 50000.0)
    private double annualPercentageRate;    // APR for finance charges (e.g., 18.0%)

    public CreditAccount() {
        super();
        this.accountType = AccountType.CREDIT;
    }

    public CreditAccount(String accountId, String userId, String accountName,
                         double initialBalance, double creditLimit, double annualPercentageRate) {
        super(accountId, userId, accountName, AccountType.CREDIT, initialBalance);
        if (creditLimit <= 0) {
            throw new IllegalArgumentException("Credit limit must be greater than zero.");
        }
        if (annualPercentageRate < 0) {
            throw new IllegalArgumentException("APR cannot be negative.");
        }
        if (initialBalance > creditLimit) {
            throw new IllegalArgumentException("Initial debt balance cannot exceed credit limit.");
        }
        this.creditLimit = creditLimit;
        this.annualPercentageRate = annualPercentageRate;
    }

    /**
     * Charging an expense to the credit card increases the balance (debt).
     */
    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }
        if ((this.balance + amount) > this.creditLimit) {
            return false; // Exceeds available credit line
        }
        this.balance += amount;
        return true;
    }

    /**
     * Making a payment to the credit card decreases the balance (repaying debt).
     */
    @Override
    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.balance = Math.max(0.0, this.balance - amount);
        return true;
    }

    @Override
    public double calculateMonthlyInterest() {
        if (this.balance <= 0) return 0.0;
        return (this.balance * (this.annualPercentageRate / 100.0)) / 12.0;
    }

    @Override
    public double getAvailableFunds() {
        return Math.max(0.0, this.creditLimit - this.balance);
    }

    @Override
    public String getAccountSummary() {
        return String.format("Credit: %s | Debt Owed: ₹%,.2f | Limit: ₹%,.2f | APR: %.2f%% | Avail Credit: ₹%,.2f",
                accountName, balance, creditLimit, annualPercentageRate, getAvailableFunds());
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getAnnualPercentageRate() {
        return annualPercentageRate;
    }

    public void setAnnualPercentageRate(double annualPercentageRate) {
        this.annualPercentageRate = annualPercentageRate;
    }
}
