package fintrack.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Observer that monitors budget threshold events.
 * Displays immediate console notifications and maintains a notification history.
 */
public class BudgetObserver implements AlertObserver {
    private final List<AlertEvent> budgetAlerts = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onAlert(AlertEvent event) {
        if (event.getType() == AlertEvent.EventType.BUDGET_WARNING ||
            event.getType() == AlertEvent.EventType.BUDGET_BREACHED) {

            budgetAlerts.add(event);

            String prefix = (event.getType() == AlertEvent.EventType.BUDGET_BREACHED)
                    ? "\u001B[31m[CRITICAL BUDGET ALERT]\u001B[0m"
                    : "\u001B[33m[BUDGET WARNING]\u001B[0m";

            System.out.println();
            System.out.println(prefix + " " + event.getTitle());
            System.out.println("   └─> " + event.getMessage());
            System.out.println();
        }
    }

    public List<AlertEvent> getBudgetAlerts() {
        return new ArrayList<>(budgetAlerts);
    }

    public void clearAlerts() {
        budgetAlerts.clear();
    }
}
