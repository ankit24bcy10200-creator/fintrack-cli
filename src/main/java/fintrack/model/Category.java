package fintrack.model;

/**
 * Spending and income classification categories.
 */
public enum Category {
    FOOD("Food & Dining"),
    RENT("Housing & Rent"),
    ENTERTAINMENT("Entertainment & Leisure"),
    UTILITIES("Bills & Utilities"),
    HEALTHCARE("Medical & Healthcare"),
    SHOPPING("Shopping & Electronics"),
    TRAVEL("Travel & Commute"),
    SALARY("Salary & Wages"),
    INVESTMENT("Investments & Dividends"),
    OTHER("Other Miscellaneous");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromString(String text) {
        if (text == null) return OTHER;
        for (Category c : Category.values()) {
            if (c.name().equalsIgnoreCase(text.trim()) || c.displayName.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return OTHER;
    }
}
