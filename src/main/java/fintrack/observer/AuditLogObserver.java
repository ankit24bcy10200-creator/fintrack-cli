package fintrack.observer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Concrete Observer that appends all critical security and financial events
 * to a local persistent audit log file (data/audit.log).
 */
public class AuditLogObserver implements AlertObserver {
    private final String logFilePath;

    public AuditLogObserver() {
        this("data/audit.log");
    }

    public AuditLogObserver(String logFilePath) {
        this.logFilePath = logFilePath;
        try {
            Files.createDirectories(Paths.get(logFilePath).getParent());
        } catch (Exception ignored) {
        }
    }

    @Override
    public synchronized void onAlert(AlertEvent event) {
        try (FileWriter fw = new FileWriter(logFilePath, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("[%s] [%s] %s - %s%n",
                    event.getTimestamp(),
                    event.getType(),
                    event.getTitle(),
                    event.getMessage());
        } catch (IOException e) {
            System.err.println("Audit log failure: " + e.getMessage());
        }
    }
}
