package fintrack.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Model representing a registered user.
 * Encapsulates credentials with salted password hash (never plaintext).
 */
public class User {
    private String userId;
    private String username;
    private String saltHex;
    private String passwordHashHex;
    private String fullName;
    private String createdAt;
    private String activeStrategy; // "Z_SCORE" or "IQR"
    private double zScoreThreshold; // default 2.5
    private double iqrMultiplier;   // default 1.5

    public User() {
    }

    public User(String username, String saltHex, String passwordHashHex, String fullName) {
        this.userId = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.username = username;
        this.saltHex = saltHex;
        this.passwordHashHex = passwordHashHex;
        this.fullName = fullName;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.activeStrategy = "Z_SCORE";
        this.zScoreThreshold = 2.5;
        this.iqrMultiplier = 1.5;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSaltHex() {
        return saltHex;
    }

    public void setSaltHex(String saltHex) {
        this.saltHex = saltHex;
    }

    public String getPasswordHashHex() {
        return passwordHashHex;
    }

    public void setPasswordHashHex(String passwordHashHex) {
        this.passwordHashHex = passwordHashHex;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getActiveStrategy() {
        return activeStrategy != null ? activeStrategy : "Z_SCORE";
    }

    public void setActiveStrategy(String activeStrategy) {
        this.activeStrategy = activeStrategy;
    }

    public double getZScoreThreshold() {
        return zScoreThreshold > 0 ? zScoreThreshold : 2.5;
    }

    public void setZScoreThreshold(double zScoreThreshold) {
        this.zScoreThreshold = zScoreThreshold;
    }

    public double getIqrMultiplier() {
        return iqrMultiplier > 0 ? iqrMultiplier : 1.5;
    }

    public void setIqrMultiplier(double iqrMultiplier) {
        this.iqrMultiplier = iqrMultiplier;
    }

    @Override
    public String toString() {
        return String.format("User: %s (%s)", fullName, username);
    }
}
