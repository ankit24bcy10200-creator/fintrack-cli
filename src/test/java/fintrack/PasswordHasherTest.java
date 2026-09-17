package fintrack;

import fintrack.util.PasswordHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for cryptographic salt-hashing and constant-time password verification.
 */
public class PasswordHasherTest {

    @Test
    @DisplayName("Password verification succeeds for correct password")
    public void testPasswordHashAndVerifySuccess() {
        String password = "SuperSecretPassword123!";
        byte[] salt = PasswordHasher.generateSalt();
        String saltHex = PasswordHasher.bytesToHex(salt);
        String hashHex = PasswordHasher.hashPassword(password, salt);

        assertTrue(PasswordHasher.verifyPassword(password, saltHex, hashHex));
    }

    @Test
    @DisplayName("Password verification fails for incorrect password")
    public void testPasswordHashAndVerifyFailure() {
        String password = "CorrectPassword123";
        byte[] salt = PasswordHasher.generateSalt();
        String saltHex = PasswordHasher.bytesToHex(salt);
        String hashHex = PasswordHasher.hashPassword(password, salt);

        assertFalse(PasswordHasher.verifyPassword("WrongPassword", saltHex, hashHex));
    }

    @Test
    @DisplayName("Identical passwords with different salts yield different hashes (rainbow table defense)")
    public void testSaltUniqueness() {
        String password = "CommonPassword999";
        byte[] salt1 = PasswordHasher.generateSalt();
        byte[] salt2 = PasswordHasher.generateSalt();

        String hash1 = PasswordHasher.hashPassword(password, salt1);
        String hash2 = PasswordHasher.hashPassword(password, salt2);

        assertNotEquals(hash1, hash2, "Hashes of the same password must differ with distinct salts");
    }
}
