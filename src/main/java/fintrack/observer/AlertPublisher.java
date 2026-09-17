package fintrack.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject/Publisher in the GoF Observer pattern.
 * Manages subscriber registration and notifies observers when events are dispatched.
 */
public class AlertPublisher {
    private final List<AlertObserver> observers = new CopyOnWriteArrayList<>();

    /**
     * Attaches an observer to receive notifications.
     */
    public void attach(AlertObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Detaches an observer.
     */
    public void detach(AlertObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    /**
     * Publishes an event to all registered observers.
     */
    public void notifyObservers(AlertEvent event) {
        for (AlertObserver observer : observers) {
            try {
                observer.onAlert(event);
            } catch (Exception e) {
                System.err.println("Error notifying observer: " + e.getMessage());
            }
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}
