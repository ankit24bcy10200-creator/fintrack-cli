package fintrack.strategy;

import fintrack.model.AnomalyResult;
import fintrack.model.Transaction;

import java.util.List;

/**
 * Strategy interface for statistical anomaly detection algorithms.
 * Demonstrates the Strategy pattern, enabling pluggable algorithms at runtime.
 */
public interface AnomalyDetectionStrategy {

    /**
     * Returns the unique identifier/name of this strategy.
     */
    String getStrategyName();

    /**
     * Returns a human-friendly description of the algorithm.
     */
    String getDescription();

    /**
     * Evaluates a single transaction against historical baseline transactions.
     *
     * @param target The transaction being tested
     * @param baselineHistory Historical transactions to compute statistical baseline from
     * @param sensitivity Statistical sensitivity threshold (e.g. Z-score cutoff or IQR multiplier)
     * @return Evaluation result with anomaly flag, score, and explanation
     */
    AnomalyResult evaluate(Transaction target, List<Transaction> baselineHistory, double sensitivity);

    /**
     * Scans a batch of transactions and identifies all anomalous entries.
     *
     * @param transactions Historical transactions list
     * @param sensitivity Statistical sensitivity threshold
     * @return List of anomaly evaluation results
     */
    List<AnomalyResult> detectAll(List<Transaction> transactions, double sensitivity);
}
