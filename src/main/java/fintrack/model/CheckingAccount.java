package fintrack.model;

/**
 * Checking Account specialization.
 * Demonstrates Inheritance and Polymorphism.
 * Supports day-to-day spending with overdraft protection and nominal transaction fees.
 */
public class CheckingAccount extends Account {
    private double overdraftLimit;     // Maximum negative balance allowed (e.g., 5000.0)
    private double transactionFee;      // Optional per-overdraft transaction fee (e.g., 10.0)

    public CheckingAccount() {
        super();
        this.accountType = AccountType.CHECKING;
    }

    public CheckingAccount(String accountId, String userId, String accountName,
                           double initialBalance, double overdraftLimit, double transactionFee) {
        super(accountId, userId, accountName, AccountType.CHECKING, initialBalance);
        if (overdraftLimit < 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be negative.");
        }
        if (transactionFee < 0) {
            throw new IllegalArgumentException("Transaction fee cannot be negative.");
        }
        this.overdraftLimit = overdraftLimit;
        this.transactionFee = transactionFee;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }
        double effectiveDeduction = amount;
        if (this.balance < amount) {
            // Apply overdraft fee if account dips below zero
            effectiveDeduction += this.transactionFee;
        }
        if ((this.balance + this.overdraftLimit) < effectiveDeduction) {
            return false; // Exceeds overdraft ceiling
        }
        this.balance -= effectiveDeduction;
        return true;
    }

    @Override
    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.balance += amount;
        return true;
    }

    @Override
    public double calculateMonthlyInterest() {
        return 0.0; // Standard checking accounts do not earn savings yield
    }

    @Override
    public double getAvailableFunds() {
        return Math.max(0.0, this.balance + this.overdraftLimit);
    }

    @Override
    public String getAccountSummary() {
        return String.format("Checking: %s | Balance: ₹%,.2f | Overdraft Limit: ₹%,.2f | Fee: ₹%,.2f | Avail: ₹%,.2f",
                accountName, balance, overdraftLimit, transactionFee, getAvailableFunds());
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public double getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(double transactionFee) {
        this.transactionFee = transactionFee;
    }
}
