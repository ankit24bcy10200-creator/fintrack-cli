package fintrack.model;

/**
 * Encapsulates the evaluation output of an AnomalyDetectionStrategy.
 */
public class AnomalyResult {
    private final Transaction transaction;
    private final boolean anomaly;
    private final double score;
    private final double threshold;
    private final String strategyName;
    private final String explanation;

    public AnomalyResult(Transaction transaction, boolean anomaly, double score,
                         double threshold, String strategyName, String explanation) {
        this.transaction = transaction;
        this.anomaly = anomaly;
        this.score = score;
        this.threshold = threshold;
        this.strategyName = strategyName;
        this.explanation = explanation;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public boolean isAnomaly() {
        return anomaly;
    }

    public double getScore() {
        return score;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return String.format("[%s] Anomaly: %b | Score: %.2f (Threshold: %.2f) | %s",
                strategyName, anomaly, score, threshold, explanation);
    }
}
