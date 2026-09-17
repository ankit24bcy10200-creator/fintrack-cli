package fintrack.service;

import fintrack.model.Category;
import fintrack.model.Transaction;
import fintrack.model.TransactionType;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service generating analytical financial reports, spending breakdowns, and monthly trends.
 */
public class ReportService {

    public static class CategoryReportItem {
        public final Category category;
        public final double totalAmount;
        public final double percentage;
        public final int transactionCount;

        public CategoryReportItem(Category category, double totalAmount, double percentage, int transactionCount) {
            this.category = category;
            this.totalAmount = totalAmount;
            this.percentage = percentage;
            this.transactionCount = transactionCount;
        }
    }

    public static class MonthlyTrendItem {
        public final String monthYear; // e.g. "2026-09"
        public final double totalIncome;
        public final double totalExpense;
        public final double netSavings;
        public final double savingsRate;

        public MonthlyTrendItem(String monthYear, double totalIncome, double totalExpense) {
            this.monthYear = monthYear;
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.netSavings = totalIncome - totalExpense;
            this.savingsRate = (totalIncome > 0) ? (this.netSavings / totalIncome) * 100.0 : 0.0;
        }
    }

    /**
     * Computes spending breakdown by category with percentage distribution.
     */
    public List<CategoryReportItem> getSpendingByCategory(List<Transaction> transactions) {
        List<Transaction> expenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());

        double grandTotal = expenses.stream().mapToDouble(Transaction::getAmount).sum();

        Map<Category, List<Transaction>> grouped = expenses.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory));

        List<CategoryReportItem> report = new ArrayList<>();
        for (Map.Entry<Category, List<Transaction>> entry : grouped.entrySet()) {
            double total = entry.getValue().stream().mapToDouble(Transaction::getAmount).sum();
            double pct = (grandTotal > 0) ? (total / grandTotal) * 100.0 : 0.0;
            report.add(new CategoryReportItem(entry.getKey(), total, pct, entry.getValue().size()));
        }

        // Sort descending by total amount
        report.sort((a, b) -> Double.compare(b.totalAmount, a.totalAmount));
        return report;
    }

    /**
     * Computes month-by-month cash flow and savings trend.
     */
    public List<MonthlyTrendItem> getMonthlyTrends(List<Transaction> transactions) {
        Map<String, List<Transaction>> byMonth = new TreeMap<>();

        for (Transaction t : transactions) {
            try {
                LocalDate d = LocalDate.parse(t.getDate());
                String key = String.format("%04d-%02d", d.getYear(), d.getMonthValue());
                byMonth.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
            } catch (Exception ignored) {
            }
        }

        List<MonthlyTrendItem> trends = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : byMonth.entrySet()) {
            double income = entry.getValue().stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .mapToDouble(Transaction::getAmount).sum();
            double expense = entry.getValue().stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .mapToDouble(Transaction::getAmount).sum();
            trends.add(new MonthlyTrendItem(entry.getKey(), income, expense));
        }

        return trends;
    }

    /**
     * Helper to render an ASCII visual bar chart for terminal output.
     * e.g., [████████░░░░] 65.0%
     */
    public static String renderProgressBar(double percentage, int barLength) {
        int filled = (int) Math.round((percentage / 100.0) * barLength);
        filled = Math.max(0, Math.min(barLength, filled));
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < filled; i++) {
            sb.append("█");
        }
        for (int i = filled; i < barLength; i++) {
            sb.append("░");
        }
        sb.append(String.format("] %5.1f%%", percentage));
        return sb.toString();
    }
}
