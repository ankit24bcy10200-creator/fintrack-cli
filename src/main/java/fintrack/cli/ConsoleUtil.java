package fintrack.cli;

import fintrack.util.InputValidator;

import java.io.Console;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console presentation utility for formatted ANSI output, borders, and safe user input prompts.
 */
public class ConsoleUtil {

    // ANSI Colors
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void printHeader(String title) {
        System.out.println();
        System.out.println(CYAN + BOLD + "================================================================================" + RESET);
        System.out.println(CYAN + BOLD + "   " + title.toUpperCase() + RESET);
        System.out.println(CYAN + BOLD + "================================================================================" + RESET);
    }

    public static void printBanner() {
        System.out.println(CYAN + BOLD +
                "███████╗██╗███╗   ██╗████████╗██████╗  █████╗  ██████╗██╗  ██╗     ██████╗██╗     ██╗\n" +
                "██╔════╝██║████╗  ██║╚══██╔══╝██╔══██╗██╔══██╗██╔════╝██║ ██╔╝    ██╔════╝██║     ██║\n" +
                "█████╗  ██║██╔██╗ ██║   ██║   ██████╔╝███████║██║     █████╔╝     ██║     ██║     ██║\n" +
                "██╔══╝  ██║██║╚██╗██║   ██║   ██╔══██╗██╔══██║██║     ██╔═██╗     ██║     ██║     ██║\n" +
                "██║     ██║██║ ╚████║   ██║   ██║  ██║██║  ██║╚██████╗██║  ██╗    ╚██████╗███████╗██║\n" +
                "╚═╝     ╚═╝╚═╝  ╚═══╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝     ╚═════╝╚══════╝╚═╝\n" +
                "             >> Personal Finance Manager & Statistical Anomaly Engine <<" + RESET);
        System.out.println(WHITE + "                        Version 1.0.0 | Core Java OOP" + RESET);
        System.out.println();
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "✔ " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "✖ " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(CYAN + "ℹ " + message + RESET);
    }

    public static String readString(String prompt) {
        while (true) {
            System.out.print(BOLD + prompt + ": " + RESET);
            if (!SCANNER.hasNextLine()) {
                System.out.println();
                System.exit(0);
            }
            String line = SCANNER.nextLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
            printWarning("Input cannot be empty. Please re-enter.");
        }
    }

    public static String readOptionalString(String prompt, String defaultValue) {
        System.out.print(BOLD + prompt + " [" + defaultValue + "]: " + RESET);
        if (!SCANNER.hasNextLine()) {
            return defaultValue;
        }
        String line = SCANNER.nextLine();
        if (line == null || line.trim().isEmpty()) {
            return defaultValue;
        }
        return line.trim();
    }

    public static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(BOLD + prompt + ": " + RESET);
            if (!SCANNER.hasNextLine()) {
                System.out.println();
                System.exit(0);
            }
            String line = SCANNER.nextLine();
            if (InputValidator.isValidPositiveDouble(line)) {
                return Double.parseDouble(line.trim());
            }
            printWarning("Invalid amount. Please enter a positive number (e.g. 1500.50).");
        }
    }

    public static double readNonNegativeDouble(String prompt) {
        while (true) {
            System.out.print(BOLD + prompt + ": " + RESET);
            if (!SCANNER.hasNextLine()) {
                System.out.println();
                System.exit(0);
            }
            String line = SCANNER.nextLine();
            if (InputValidator.isValidNonNegativeDouble(line)) {
                return Double.parseDouble(line.trim());
            }
            printWarning("Invalid input. Please enter a non-negative number (e.g. 0 or 4.5).");
        }
    }

    public static int readChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(BOLD + prompt + " (" + min + "-" + max + "): " + RESET);
            if (!SCANNER.hasNextLine()) {
                System.out.println();
                System.exit(0);
            }
            String line = SCANNER.nextLine();
            if (InputValidator.isValidIntegerInRange(line, min, max)) {
                return Integer.parseInt(line.trim());
            }
            printWarning("Please enter a valid option between " + min + " and " + max + ".");
        }
    }

    public static String readDate(String prompt) {
        String today = LocalDate.now().toString();
        while (true) {
            System.out.print(BOLD + prompt + " [Default: " + today + "]: " + RESET);
            if (!SCANNER.hasNextLine()) {
                return today;
            }
            String line = SCANNER.nextLine();
            if (line == null || line.trim().isEmpty()) {
                return today;
            }
            if (InputValidator.isValidDate(line)) {
                return line.trim();
            }
            printWarning("Invalid date format. Expected YYYY-MM-DD (e.g. 2026-09-18).");
        }
    }

    public static String readPassword(String prompt) {
        Console console = System.console();
        // If interactive console is attached and not redirected
        if (console != null) {
            try {
                char[] passChars = console.readPassword(BOLD + prompt + ": " + RESET);
                if (passChars != null && passChars.length > 0) {
                    return new String(passChars);
                }
            } catch (Exception ignored) {
            }
        }
        // Fallback for IDE terminals or non-interactive redirection
        return readString(prompt);
    }

    public static void pause() {
        System.out.print(WHITE + "\nPress Enter to continue..." + RESET);
        if (SCANNER.hasNextLine()) {
            SCANNER.nextLine();
        }
    }

    /**
     * Renders a cleanly formatted ASCII table to the console.
     */
    public static void renderTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) return;

        int cols = headers.length;
        int[] colWidths = new int[cols];

        for (int i = 0; i < cols; i++) {
            colWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(cols, row.length); i++) {
                if (row[i] != null) {
                    // Strip ANSI codes for length calculation
                    String stripped = row[i].replaceAll("\u001B\\[[;\\d]*m", "");
                    colWidths[i] = Math.max(colWidths[i], stripped.length());
                }
            }
        }

        // Top border
        printTableDivider(colWidths, "┌", "┬", "┐");

        // Header
        System.out.print("│");
        for (int i = 0; i < cols; i++) {
            System.out.printf(" %s%-"+colWidths[i]+"s%s │", BOLD, headers[i], RESET);
        }
        System.out.println();

        // Header separator
        printTableDivider(colWidths, "├", "┼", "┤");

        // Rows
        if (rows.isEmpty()) {
            System.out.print("│");
            int totalWidth = 0;
            for (int w : colWidths) totalWidth += w + 3;
            totalWidth -= 1;
            String emptyMsg = "No records found.";
            System.out.printf(" %-" + totalWidth + "s│\n", emptyMsg);
        } else {
            for (String[] row : rows) {
                System.out.print("│");
                for (int i = 0; i < cols; i++) {
                    String val = (i < row.length && row[i] != null) ? row[i] : "";
                    String stripped = val.replaceAll("\u001B\\[[;\\d]*m", "");
                    int padding = colWidths[i] - stripped.length();
                    System.out.print(" " + val + " ".repeat(Math.max(0, padding)) + " │");
                }
                System.out.println();
            }
        }

        // Bottom border
        printTableDivider(colWidths, "└", "┴", "┘");
    }

    private static void printTableDivider(int[] colWidths, String left, String mid, String right) {
        System.out.print(left);
        for (int i = 0; i < colWidths.length; i++) {
            System.out.print("─".repeat(colWidths[i] + 2));
            if (i < colWidths.length - 1) {
                System.out.print(mid);
            }
        }
        System.out.println(right);
    }
}
