package fintrack.observer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Event payload dispatched to registered AlertObservers.
 */
public class AlertEvent {

    public enum EventType {
        BUDGET_WARNING,      // Budget reached >= 80%
        BUDGET_BREACHED,     // Budget exceeded 100%
        ANOMALY_DETECTED,    // Transaction flagged by statistical engine
        SECURITY_ALERT,      // Failed logins, password changes
        SYSTEM_INFO          // Account creations, exports
    }

    private final EventType type;
    private final String title;
    private final String message;
    private final Object payload;
    private final String timestamp;

    public AlertEvent(EventType type, String title, String message, Object payload) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.payload = payload;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public EventType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Object getPayload() {
        return payload;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", type, title, message);
    }
}
