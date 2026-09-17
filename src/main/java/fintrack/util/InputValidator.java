package fintrack.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class for rigorous sanitization and validation of user input.
 * Ensures the CLI application never crashes on malformed input.
 */
public class InputValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.trim().length() >= 6;
    }

    public static boolean isValidPositiveDouble(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        try {
            double val = Double.parseDouble(input.trim());
            return val > 0.0 && !Double.isInfinite(val) && !Double.isNaN(val);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidNonNegativeDouble(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        try {
            double val = Double.parseDouble(input.trim());
            return val >= 0.0 && !Double.isInfinite(val) && !Double.isNaN(val);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        try {
            LocalDate parsed = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            // Allow dates from year 2000 up to 1 year in the future
            return !parsed.isBefore(LocalDate.of(2000, 1, 1)) &&
                   !parsed.isAfter(LocalDate.now().plusYears(1));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidIntegerInRange(String input, int min, int max) {
        if (input == null || input.trim().isEmpty()) return false;
        try {
            int val = Integer.parseInt(input.trim());
            return val >= min && val <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[\r\n\t]", " ");
    }
}
