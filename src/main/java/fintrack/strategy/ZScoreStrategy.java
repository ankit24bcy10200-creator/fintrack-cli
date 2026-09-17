package fintrack.strategy;

import fintrack.model.AnomalyResult;
import fintrack.model.Transaction;
import fintrack.model.TransactionType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Z-score (Standard Score) Anomaly Detection Strategy.
 * Calculates mean (μ) and standard deviation (σ) from historical expenses.
 * A transaction x is flagged if Z = (x - μ) / σ > threshold.
 */
public class ZScoreStrategy implements AnomalyDetectionStrategy {

    public static final String NAME = "Z_SCORE";
    public static final double DEFAULT_THRESHOLD = 2.5;

    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Z-Score Analysis: Flags expenses deviating by > N standard deviations from historical mean.";
    }

    @Override
    public AnomalyResult evaluate(Transaction target, List<Transaction> baselineHistory, double sensitivity) {
        if (target == null || target.getType() != TransactionType.EXPENSE) {
            return new AnomalyResult(target, false, 0.0, sensitivity, NAME, "Non-expense transactions are not evaluated.");
        }

        // Filter relevant expense history within the same category
        List<Double> amounts = baselineHistory.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategory() == target.getCategory())
                .filter(t -> !t.getTransactionId().equals(target.getTransactionId()))
                .map(Transaction::getAmount)
                .collect(Collectors.toList());

        // Fallback to all expense transactions if category history is insufficient
        if (amounts.size() < 3) {
            amounts = baselineHistory.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> !t.getTransactionId().equals(target.getTransactionId()))
                    .map(Transaction::getAmount)
                    .collect(Collectors.toList());
        }

        if (amounts.size() < 3) {
            return new AnomalyResult(target, false, 0.0, sensitivity, NAME,
                    "Insufficient baseline data (need at least 3 transactions to establish variance).");
        }

        double mean = calculateMean(amounts);
        double stdDev = calculateStdDev(amounts, mean);
        double targetAmount = target.getAmount();

        if (stdDev == 0.0) {
            if (targetAmount > mean * 1.5) {
                return new AnomalyResult(target, true, 99.0, sensitivity, NAME,
                        String.format("Deviation from identical history (Amount: ₹%,.2f vs Constant: ₹%,.2f)", targetAmount, mean));
            }
            return new AnomalyResult(target, false, 0.0, sensitivity, NAME, "Zero variance in historical spending.");
        }

        double zScore = (targetAmount - mean) / stdDev;
        boolean isAnomaly = zScore > sensitivity;

        String explanation;
        if (isAnomaly) {
            explanation = String.format("Expense ₹%,.2f exceeds category mean ₹%,.2f by %.2fσ (Threshold: %.2fσ, StdDev: ₹%,.2f)",
                    targetAmount, mean, zScore, sensitivity, stdDev);
        } else {
            explanation = String.format("Normal spending (Z-Score: %.2fσ <= %.2fσ, Mean: ₹%,.2f)",
                    zScore, sensitivity, mean);
        }

        return new AnomalyResult(target, isAnomaly, Math.max(0.0, zScore), sensitivity, NAME, explanation);
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

    private double calculateMean(List<Double> values) {
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    private double calculateStdDev(List<Double> values, double mean) {
        if (values.size() <= 1) return 0.0;
        double sumSquares = 0.0;
        for (double v : values) {
            sumSquares += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSquares / (values.size() - 1)); // Sample standard deviation
    }
}
