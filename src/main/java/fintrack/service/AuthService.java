package fintrack.service;

import fintrack.model.User;
import fintrack.observer.AlertEvent;
import fintrack.observer.AlertPublisher;
import fintrack.persistence.JsonFileStore;
import fintrack.util.InputValidator;
import fintrack.util.Logger;
import fintrack.util.PasswordHasher;

import java.util.Optional;

/**
 * Service managing user authentication, registration, and active session state.
 */
public class AuthService {

    private final JsonFileStore fileStore;
    private final AlertPublisher alertPublisher;
    private User currentUser;

    public AuthService(JsonFileStore fileStore, AlertPublisher alertPublisher) {
        this.fileStore = fileStore;
        this.alertPublisher = alertPublisher;
    }

    /**
     * Registers a new user with salted SHA-256 password hashing.
     */
    public synchronized boolean register(String username, String password, String fullName) {
        if (!InputValidator.isValidUsername(username)) {
            throw new IllegalArgumentException("Username must be 3-20 alphanumeric characters or underscores.");
        }
        if (!InputValidator.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }

        JsonFileStore.DataContainer data = fileStore.loadData();
        boolean exists = data.users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username.trim()));
        if (exists) {
            return false; // Username already taken
        }

        byte[] salt = PasswordHasher.generateSalt();
        String saltHex = PasswordHasher.bytesToHex(salt);
        String hashHex = PasswordHasher.hashPassword(password, salt);

        User newUser = new User(username.trim(), saltHex, hashHex, fullName.trim());
        data.users.add(newUser);
        fileStore.saveData(data);

        Logger.getInstance().info("Registered new user: " + username);
        alertPublisher.notifyObservers(new AlertEvent(
                AlertEvent.EventType.SYSTEM_INFO,
                "User Registered",
                "New account created for " + fullName + " (@" + username + ")",
                newUser
        ));
        return true;
    }

    /**
     * Authenticates a user by comparing salted hash.
     */
    public synchronized boolean login(String username, String password) {
        if (username == null || password == null) return false;

        JsonFileStore.DataContainer data = fileStore.loadData();
        Optional<User> userOpt = data.users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username.trim()))
                .findFirst();

        if (userOpt.isEmpty()) {
            Logger.getInstance().warn("Login failed: username not found (" + username + ")");
            return false;
        }

        User user = userOpt.get();
        boolean matches = PasswordHasher.verifyPassword(password, user.getSaltHex(), user.getPasswordHashHex());

        if (matches) {
            this.currentUser = user;
            Logger.getInstance().info("User logged in successfully: " + username);
            return true;
        } else {
            Logger.getInstance().warn("Failed login attempt for user: " + username);
            alertPublisher.notifyObservers(new AlertEvent(
                    AlertEvent.EventType.SECURITY_ALERT,
                    "Failed Login Attempt",
                    "Unsuccessful login attempt detected for user @" + username,
                    username
            ));
            return false;
        }
    }

    public synchronized void logout() {
        if (this.currentUser != null) {
            Logger.getInstance().info("User logged out: " + this.currentUser.getUsername());
            this.currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized void updateUserPreferences(String strategy, double threshold) {
        if (currentUser == null) return;

        JsonFileStore.DataContainer data = fileStore.loadData();
        for (User u : data.users) {
            if (u.getUserId().equals(currentUser.getUserId())) {
                u.setActiveStrategy(strategy);
                if ("Z_SCORE".equalsIgnoreCase(strategy)) {
                    u.setZScoreThreshold(threshold);
                } else {
                    u.setIqrMultiplier(threshold);
                }
                this.currentUser = u;
                break;
            }
        }
        fileStore.saveData(data);
    }
}
