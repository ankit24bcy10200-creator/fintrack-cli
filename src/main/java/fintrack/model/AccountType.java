package fintrack.model;

/**
 * Enumeration of supported bank account types in FinTrack.
 */
public enum AccountType {
    SAVINGS("Savings Account", "Earns interest with minimum balance requirements"),
    CHECKING("Checking Account", "Day-to-day spending with overdraft protection"),
    CREDIT("Credit Account", "Revolving credit line with monthly finance charge calculation");

    private final String displayName;
    private final String description;

    AccountType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
