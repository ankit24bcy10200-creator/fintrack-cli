package fintrack.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Observer that monitors statistical anomaly events.
 * Alerts user immediately upon entry of suspicious or abnormal transactions.
 */
public class AnomalyObserver implements AlertObserver {
    private final List<AlertEvent> anomalyAlerts = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onAlert(AlertEvent event) {
        if (event.getType() == AlertEvent.EventType.ANOMALY_DETECTED) {
            anomalyAlerts.add(event);

            System.out.println();
            System.out.println("\u001B[1;35m[ANOMALY ENGINE ALERT] ⚠️ Statistical Outlier Detected!\u001B[0m");
            System.out.println("   Title : " + event.getTitle());
            System.out.println("   Detail: " + event.getMessage());
            System.out.println("   Action: Transaction recorded but flagged for review in the Anomaly Report.");
            System.out.println();
        }
    }

    public List<AlertEvent> getAnomalyAlerts() {
        return new ArrayList<>(anomalyAlerts);
    }

    public void clearAlerts() {
        anomalyAlerts.clear();
    }
}
