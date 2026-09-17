package fintrack.persistence;

import fintrack.model.Budget;
import fintrack.model.Transaction;
import fintrack.util.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility for exporting transaction histories and financial reports to standard CSV files.
 */
public class CsvExporter {

    private static final String EXPORT_DIR = "exports";

    static {
        try {
            Files.createDirectories(Paths.get(EXPORT_DIR));
        } catch (IOException ignored) {
        }
    }

    /**
     * Exports a list of transactions to a CSV file.
     *
     * @param transactions Transactions to export
     * @param username Active user's username for naming
     * @return Path to the generated CSV file
     */
    public static String exportTransactions(List<Transaction> transactions, String username) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s/transactions_%s_%s.csv", EXPORT_DIR, username, timestamp);
        Path path = Paths.get(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("TransactionID,Date,Timestamp,AccountID,Type,Category,Amount,Description,FlaggedAnomaly,AnomalyReason\n");

            for (Transaction t : transactions) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,\"%s\",%b,\"%s\"\n",
                        escapeCsv(t.getTransactionId()),
                        escapeCsv(t.getDate()),
                        escapeCsv(t.getTimestamp()),
                        escapeCsv(t.getAccountId()),
                        t.getType().name(),
                        t.getCategory().name(),
                        t.getAmount(),
                        escapeCsv(t.getDescription()),
                        t.isFlaggedAsAnomaly(),
                        escapeCsv(t.getAnomalyReason())));
            }

            Logger.getInstance().info("Exported " + transactions.size() + " transactions to " + fileName);
            return path.toAbsolutePath().toString();
        } catch (IOException e) {
            Logger.getInstance().error("Failed to export transactions to CSV", e);
            return null;
        }
    }

    /**
     * Exports budget summaries with spent vs limit to CSV.
     */
    public static String exportBudgetReport(List<Budget> budgets, Map<String, Double> categorySpending, String username) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s/budget_report_%s_%s.csv", EXPORT_DIR, username, timestamp);
        Path path = Paths.get(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("BudgetID,Category,Month,Year,MonthlyLimit,ActualSpent,Remaining,UsagePercent,Status\n");

            for (Budget b : budgets) {
                double spent = categorySpending.getOrDefault(b.getCategory().name(), 0.0);
                double remaining = Math.max(0.0, b.getMonthlyLimit() - spent);
                double pct = (b.getMonthlyLimit() > 0) ? (spent / b.getMonthlyLimit()) * 100.0 : 0.0;
                String status = (spent > b.getMonthlyLimit()) ? "BREACHED" : (pct >= 80.0 ? "WARNING" : "OK");

                writer.write(String.format("\"%s\",\"%s\",%d,%d,%.2f,%.2f,%.2f,%.1f%%,\"%s\"\n",
                        escapeCsv(b.getBudgetId()),
                        escapeCsv(b.getCategory().getDisplayName()),
                        b.getMonth(),
                        b.getYear(),
                        b.getMonthlyLimit(),
                        spent,
                        remaining,
                        pct,
                        status));
            }

            Logger.getInstance().info("Exported budget report to " + fileName);
            return path.toAbsolutePath().toString();
        } catch (IOException e) {
            Logger.getInstance().error("Failed to export budget report to CSV", e);
            return null;
        }
    }

    private static String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}
