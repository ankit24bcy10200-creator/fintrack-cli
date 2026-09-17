package fintrack.strategy;

import fintrack.model.AnomalyResult;
import fintrack.model.Transaction;
import fintrack.model.TransactionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interquartile Range (IQR) Anomaly Detection Strategy.
 * Non-parametric algorithm that calculates Q1 (25th percentile), Q3 (75th percentile),
 * and IQR = Q3 - Q1. An expense is flagged if x > Q3 + (k * IQR).
 */
public class IQRStrategy implements AnomalyDetectionStrategy {

    public static final String NAME = "IQR";
    public static final double DEFAULT_MULTIPLIER = 1.5;

    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "IQR Analysis: Non-parametric Tukey fence method; flags expenses above Q3 + (k * IQR).";
    }

    @Override
    public AnomalyResult evaluate(Transaction target, List<Transaction> baselineHistory, double sensitivity) {
        if (target == null || target.getType() != TransactionType.EXPENSE) {
            return new AnomalyResult(target, false, 0.0, sensitivity, NAME, "Non-expense transactions are not evaluated.");
        }

        // Filter category expense history
        List<Double> amounts = baselineHistory.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategory() == target.getCategory())
                .filter(t -> !t.getTransactionId().equals(target.getTransactionId()))
                .map(Transaction::getAmount)
                .sorted()
                .collect(Collectors.toList());

        // Fallback to overall expenses if category history is limited
        if (amounts.size() < 4) {
            amounts = baselineHistory.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> !t.getTransactionId().equals(target.getTransactionId()))
                    .map(Transaction::getAmount)
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (amounts.size() < 4) {
            return new AnomalyResult(target, false, 0.0, sensitivity, NAME,
                    "Insufficient baseline data (need at least 4 transactions to calculate quartiles).");
        }

        double q1 = calculatePercentile(amounts, 25.0);
        double q3 = calculatePercentile(amounts, 75.0);
        double iqr = q3 - q1;
        double upperFence = q3 + (sensitivity * iqr);
        double targetAmount = target.getAmount();

        boolean isAnomaly = targetAmount > upperFence;
        double score = (iqr > 0) ? ((targetAmount - q3) / iqr) : (targetAmount > q3 ? 99.0 : 0.0);

        String explanation;
        if (isAnomaly) {
            explanation = String.format("Expense ₹%,.2f breaches IQR upper fence ₹%,.2f (Q1: ₹%,.2f, Q3: ₹%,.2f, IQR: ₹%,.2f, k: %.1f)",
                    targetAmount, upperFence, q1, q3, iqr, sensitivity);
        } else {
            explanation = String.format("Normal spending within IQR fence (Amount: ₹%,.2f <= ₹%,.2f, Q3: ₹%,.2f)",
                    targetAmount, upperFence, q3);
        }

        return new AnomalyResult(target, isAnomaly, Math.max(0.0, score), sensitivity, NAME, explanation);
    }

    @Override
    public List<AnomalyResult> detectAll(List<Transaction> transactions, double sensitivity) {
        List<AnomalyResult> results = new ArrayList<>();
        List<Transaction> expenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());

        for (Transaction t : expenses) {
            results.add(evaluate(t, expenses, sensitivity));
        }
        return results;
    }

    /**
     * Computes linear interpolation percentile for given sorted values.
     */
    private double calculatePercentile(List<Double> sorted, double percentile) {
        if (sorted.isEmpty()) return 0.0;
        if (sorted.size() == 1) return sorted.get(0);

        double index = (percentile / 100.0) * (sorted.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);

        if (lower == upper) {
            return sorted.get(lower);
        }
        double weight = index - lower;
        return sorted.get(lower) * (1.0 - weight) + sorted.get(upper) * weight;
    }
}
