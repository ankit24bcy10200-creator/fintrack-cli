package fintrack.service;

import fintrack.model.AnomalyResult;
import fintrack.model.Transaction;
import fintrack.strategy.AnomalyDetectionStrategy;
import fintrack.strategy.IQRStrategy;
import fintrack.strategy.ZScoreStrategy;
import fintrack.util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service orchestrating statistical anomaly detection algorithms.
 * Implements runtime Strategy selection and execution.
 */
public class AnomalyService {

    private final Map<String, AnomalyDetectionStrategy> strategies = new HashMap<>();
    private AnomalyDetectionStrategy activeStrategy;
    private double currentSensitivity;

    public AnomalyService() {
        ZScoreStrategy zScore = new ZScoreStrategy();
        IQRStrategy iqr = new IQRStrategy();

        strategies.put(ZScoreStrategy.NAME, zScore);
        strategies.put(IQRStrategy.NAME, iqr);

        // Default to Z-Score with 2.5 threshold
        this.activeStrategy = zScore;
        this.currentSensitivity = ZScoreStrategy.DEFAULT_THRESHOLD;
    }

    public synchronized void setActiveStrategy(String strategyName, double sensitivity) {
        AnomalyDetectionStrategy strategy = strategies.get(strategyName.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown anomaly strategy: " + strategyName);
        }
        this.activeStrategy = strategy;
        this.currentSensitivity = (sensitivity > 0) ? sensitivity :
                (ZScoreStrategy.NAME.equalsIgnoreCase(strategyName) ? ZScoreStrategy.DEFAULT_THRESHOLD : IQRStrategy.DEFAULT_MULTIPLIER);

        Logger.getInstance().info(String.format("Switched anomaly strategy to %s (Sensitivity: %.2f)",
                activeStrategy.getStrategyName(), currentSensitivity));
    }

    public AnomalyDetectionStrategy getActiveStrategy() {
        return activeStrategy;
    }

    public double getCurrentSensitivity() {
        return currentSensitivity;
    }

    public AnomalyResult evaluateTransaction(Transaction target, List<Transaction> history) {
        return activeStrategy.evaluate(target, history, currentSensitivity);
    }

    public List<AnomalyResult> detectAll(List<Transaction> transactions) {
        return activeStrategy.detectAll(transactions, currentSensitivity);
    }

    public Map<String, AnomalyDetectionStrategy> getAvailableStrategies() {
        return new HashMap<>(strategies);
    }
}
