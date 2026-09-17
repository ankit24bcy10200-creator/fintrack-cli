package fintrack.model;

/**
 * Types of financial transactions supported.
 */
public enum TransactionType {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
