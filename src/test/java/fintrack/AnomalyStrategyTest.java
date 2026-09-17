package fintrack;

import fintrack.model.AnomalyResult;
import fintrack.model.Category;
import fintrack.model.Transaction;
import fintrack.model.TransactionType;
import fintrack.strategy.IQRStrategy;
import fintrack.strategy.ZScoreStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for statistical anomaly detection strategies (Z-Score & IQR).
 */
public class AnomalyStrategyTest {

    private ZScoreStrategy zScoreStrategy;
    private IQRStrategy iqrStrategy;
    private List<Transaction> baseline;

    @BeforeEach
    public void setUp() {
        zScoreStrategy = new ZScoreStrategy();
        iqrStrategy = new IQRStrategy();
        baseline = new ArrayList<>();

        // Create baseline of normal Food expenses around ₹500
        double[] amounts = {480.0, 520.0, 490.0, 510.0, 500.0, 530.0, 470.0};
        for (int i = 0; i < amounts.length; i++) {
            Transaction t = new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE,
                    Category.FOOD, amounts[i], "2026-09-0" + (i + 1), "Grocery " + (i + 1));
            baseline.add(t);
        }
    }

    @Test
    @DisplayName("Z-Score Strategy correctly flags an extreme outlier expense")
    public void testZScoreDetectsOutlier() {
        Transaction extremeTxn = new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE,
                Category.FOOD, 25000.0, "2026-09-10", "Five Star Banquet");

        AnomalyResult result = zScoreStrategy.evaluate(extremeTxn, baseline, 2.5);

        assertTrue(result.isAnomaly(), "Expense of 25000 against baseline of ~500 should be flagged as anomaly");
        assertTrue(result.getScore() > 2.5, "Z-Score should exceed 2.5 threshold");
        assertEquals("Z_SCORE", result.getStrategyName());
        assertTrue(result.getExplanation().contains("exceeds category mean"));
    }

    @Test
    @DisplayName("Z-Score Strategy allows normal spending within threshold")
    public void testZScoreNormalSpending() {
        Transaction normalTxn = new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE,
                Category.FOOD, 525.0, "2026-09-10", "Lunch");

        AnomalyResult result = zScoreStrategy.evaluate(normalTxn, baseline, 2.5);

        assertFalse(result.isAnomaly(), "Spending 525 against ~500 mean should not be an anomaly");
        assertTrue(result.getScore() < 2.5, "Z-score should be well within 2.5");
    }

    @Test
    @DisplayName("Z-Score handles small sample size (< 3) gracefully")
    public void testZScoreInsufficientData() {
        List<Transaction> smallHistory = new ArrayList<>();
        smallHistory.add(new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE, Category.FOOD, 500.0, "2026-09-01", "Coffee"));

        Transaction target = new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE, Category.FOOD, 5000.0, "2026-09-02", "Dinner");
        AnomalyResult result = zScoreStrategy.evaluate(target, smallHistory, 2.5);

        assertFalse(result.isAnomaly());
        assertTrue(result.getExplanation().contains("Insufficient baseline data"));
    }

    @Test
    @DisplayName("IQR Strategy correctly flags upper fence breach")
    public void testIQRDetectsOutlier() {
        Transaction outlierTxn = new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE,
                Category.FOOD, 5000.0, "2026-09-10", "Expensive Dinner");

        AnomalyResult result = iqrStrategy.evaluate(outlierTxn, baseline, 1.5);

        assertTrue(result.isAnomaly(), "5000 should exceed IQR upper fence");
        assertEquals("IQR", result.getStrategyName());
        assertTrue(result.getExplanation().contains("breaches IQR upper fence"));
    }

    @Test
    @DisplayName("IQR Strategy allows normal spending within fence")
    public void testIQRNormalSpending() {
        Transaction normalTxn = new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE,
                Category.FOOD, 510.0, "2026-09-10", "Lunch");

        AnomalyResult result = iqrStrategy.evaluate(normalTxn, baseline, 1.5);

        assertFalse(result.isAnomaly(), "510 is within normal IQR distribution");
    }

    @Test
    @DisplayName("IQR handles small sample size (< 4) without crash")
    public void testIQRInsufficientData() {
        List<Transaction> smallHistory = new ArrayList<>();
        smallHistory.add(new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE, Category.FOOD, 500.0, "2026-09-01", "Coffee"));
        smallHistory.add(new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE, Category.FOOD, 550.0, "2026-09-02", "Lunch"));

        Transaction target = new Transaction("USR-1", "ACC-1", TransactionType.EXPENSE, Category.FOOD, 10000.0, "2026-09-03", "Party");
        AnomalyResult result = iqrStrategy.evaluate(target, smallHistory, 1.5);

        assertFalse(result.isAnomaly());
        assertTrue(result.getExplanation().contains("Insufficient baseline data"));
    }
}
