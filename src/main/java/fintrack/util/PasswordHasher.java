package fintrack.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility for cryptographically secure password hashing and verification.
 * Uses SHA-256 with a 16-byte random salt and key stretching (1000 iterations).
 * Protects against rainbow tables and timing attacks.
 */
public class PasswordHasher {

    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 1000;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a random 16-byte salt.
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a plaintext password with the provided salt using stretched SHA-256.
     *
     * @param password Plaintext password
     * @param salt Cryptographic salt
     * @return Hex-encoded digest string
     */
    public static String hashPassword(String password, byte[] salt) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }
        if (salt == null || salt.length == 0) {
            throw new IllegalArgumentException("Salt cannot be null or empty.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(salt);
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Key stretching iterations
            for (int i = 0; i < ITERATIONS; i++) {
                digest.reset();
                hashed = digest.digest(hashed);
            }

            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JRE", e);
        }
    }

    /**
     * Verifies if a candidate plaintext password matches the stored salt and hash.
     * Uses constant-time comparison to protect against timing side-channel attacks.
     */
    public static boolean verifyPassword(String candidatePassword, String saltHex, String storedHashHex) {
        if (candidatePassword == null || saltHex == null || storedHashHex == null) {
            return false;
        }

        byte[] salt = hexToBytes(saltHex);
        String candidateHashHex = hashPassword(candidatePassword, salt);

        byte[] candidateHashBytes = hexToBytes(candidateHashHex);
        byte[] storedHashBytes = hexToBytes(storedHashHex);

        return MessageDigest.isEqual(candidateHashBytes, storedHashBytes);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
