package fintrack.observer;

/**
 * Observer interface in the GoF Observer pattern.
 * Subscribers implement this to handle asynchronous or event-driven alerts.
 */
public interface AlertObserver {
    /**
     * Callback triggered when an alert event is published.
     *
     * @param event The alert event containing context and payload
     */
    void onAlert(AlertEvent event);
}
