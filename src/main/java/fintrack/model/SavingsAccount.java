package fintrack.model;

/**
 * Savings Account specialization.
 * Demonstrates Inheritance and Polymorphism.
 * Enforces a minimum balance rule and calculates accrued monthly interest.
 */
public class SavingsAccount extends Account {
    private double interestRate;      // Annual interest rate (e.g., 4.5%)
    private double minimumBalance;     // Minimum balance required (e.g., 1000.0)

    public SavingsAccount() {
        super();
        this.accountType = AccountType.SAVINGS;
    }

    public SavingsAccount(String accountId, String userId, String accountName,
                          double initialBalance, double interestRate, double minimumBalance) {
        super(accountId, userId, accountName, AccountType.SAVINGS, initialBalance);
        if (interestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        }
        if (minimumBalance < 0) {
            throw new IllegalArgumentException("Minimum balance cannot be negative.");
        }
        if (initialBalance < minimumBalance) {
            throw new IllegalArgumentException("Initial balance cannot be less than minimum balance ₹" + minimumBalance);
        }
        this.interestRate = interestRate;
        this.minimumBalance = minimumBalance;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }
        if ((this.balance - amount) < this.minimumBalance) {
            return false; // Breach of minimum balance constraint
        }
        this.balance -= amount;
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
        if (this.balance <= 0) return 0.0;
        return (this.balance * (this.interestRate / 100.0)) / 12.0;
    }

    @Override
    public double getAvailableFunds() {
        return Math.max(0.0, this.balance - this.minimumBalance);
    }

    @Override
    public String getAccountSummary() {
        return String.format("Savings: %s | Balance: ₹%,.2f | Min Balance: ₹%,.2f | Int Rate: %.2f%% | Avail: ₹%,.2f",
                accountName, balance, minimumBalance, interestRate, getAvailableFunds());
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }
}
