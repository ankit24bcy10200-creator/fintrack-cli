package fintrack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Model representing a financial transaction.
 * Demonstrates Encapsulation.
 */
public class Transaction {
    private String transactionId;
    private String userId;
    private String accountId;
    private String targetAccountId; // Used only for TRANSFER transactions
    private TransactionType type;
    private Category category;
    private double amount;
    private String date; // YYYY-MM-DD
    private String timestamp; // YYYY-MM-DD HH:mm:ss
    private String description;
    private boolean flaggedAsAnomaly;
    private String anomalyReason;

    public Transaction() {
        // Default constructor for serialization
    }

    public Transaction(String userId, String accountId, TransactionType type, Category category,
                       double amount, String date, String description) {
        this.transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.userId = userId;
        this.accountId = accountId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = (date != null && !date.trim().isEmpty()) ? date : LocalDate.now().toString();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.description = (description != null) ? description.trim() : "";
        this.flaggedAsAnomaly = false;
        this.anomalyReason = "";
    }

    public Transaction(String transactionId, String userId, String accountId, TransactionType type,
                       Category category, double amount, String date, String description) {
        this(userId, accountId, type, category, amount, date, description);
        if (transactionId != null && !transactionId.trim().isEmpty()) {
            this.transactionId = transactionId;
        }
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(String targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFlaggedAsAnomaly() {
        return flaggedAsAnomaly;
    }

    public void setFlaggedAsAnomaly(boolean flaggedAsAnomaly) {
        this.flaggedAsAnomaly = flaggedAsAnomaly;
    }

    public String getAnomalyReason() {
        return anomalyReason;
    }

    public void setAnomalyReason(String anomalyReason) {
        this.anomalyReason = anomalyReason;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | ₹%,.2f | %s%s",
                transactionId, date, type, category, amount, description,
                flaggedAsAnomaly ? " [ANOMALY FLAGGED]" : "");
    }
}
