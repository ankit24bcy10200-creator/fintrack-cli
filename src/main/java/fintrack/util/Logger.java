package fintrack.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized logging manager implemented using the GoF Singleton Pattern
 * (Bill Pugh Thread-Safe Initialization-on-Demand Holder idiom).
 * Protects against multiple instantiations via reflection and serialization.
 */
public class Logger {

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    private static final String LOG_FILE = "data/fintrack.log";
    private LogLevel minimumLevel = LogLevel.INFO;
    private boolean consoleOutputEnabled = false;

    // Private constructor prevents external instantiation
    private Logger() {
        if (InstanceHolder.INSTANCE != null) {
            throw new IllegalStateException("Singleton instance already exists.");
        }
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (Exception ignored) {
        }
    }

    // Static nested class for thread-safe lazy initialization
    private static class InstanceHolder {
        private static final Logger INSTANCE = new Logger();
    }

    /**
     * Global access point for the Singleton Logger.
     */
    public static Logger getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public synchronized void setMinimumLevel(LogLevel level) {
        this.minimumLevel = level;
    }

    public synchronized void setConsoleOutputEnabled(boolean enabled) {
        this.consoleOutputEnabled = enabled;
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    private synchronized void log(LogLevel level, String message, Throwable throwable) {
        if (level.ordinal() < minimumLevel.ordinal()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String entry = String.format("[%s] [%-5s] %s", timestamp, level, message);

        if (consoleOutputEnabled) {
            System.err.println(entry);
            if (throwable != null) {
                throwable.printStackTrace(System.err);
            }
        }

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(entry);
            if (throwable != null) {
                throwable.printStackTrace(pw);
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    // Protect against deserialization creating a new instance
    protected Object readResolve() {
        return getInstance();
    }
}
