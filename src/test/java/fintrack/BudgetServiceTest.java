package fintrack;

import fintrack.model.Budget;
import fintrack.model.Category;
import fintrack.model.Transaction;
import fintrack.model.TransactionType;
import fintrack.observer.AlertEvent;
import fintrack.observer.AlertObserver;
import fintrack.observer.AlertPublisher;
import fintrack.persistence.JsonFileStore;
import fintrack.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BudgetService and threshold alerts via the Observer pattern.
 */
public class BudgetServiceTest {

    private BudgetService budgetService;
    private AlertPublisher alertPublisher;
    private List<AlertEvent> capturedEvents;
    private static final String TEST_DB = "data/test_budget_data.json";

    @BeforeEach
    public void setUp() {
        new File(TEST_DB).delete();
        JsonFileStore fileStore = new JsonFileStore(TEST_DB);
        alertPublisher = new AlertPublisher();
        capturedEvents = new ArrayList<>();

        // Test subscriber to capture emitted events
        alertPublisher.attach(new AlertObserver() {
            @Override
            public void onAlert(AlertEvent event) {
                capturedEvents.add(event);
            }
        });

        budgetService = new BudgetService(fileStore, alertPublisher);
    }

    @Test
    @DisplayName("Setting and retrieving a monthly category budget")
    public void testSetAndGetBudget() {
        Budget b = budgetService.setBudget("USR-TEST", Category.FOOD, 5000.0, 9, 2026);
        assertNotNull(b);
        assertEquals(5000.0, b.getMonthlyLimit());

        List<Budget> budgets = budgetService.getBudgetsForUser("USR-TEST", 9, 2026);
        assertEquals(1, budgets.size());
        assertEquals(Category.FOOD, budgets.get(0).getCategory());
    }

    @Test
    @DisplayName("Observer triggers BUDGET_WARNING event when spending reaches >= 80%")
    public void testBudgetWarningObserverAlert() {
        budgetService.setBudget("USR-TEST", Category.FOOD, 1000.0, 9, 2026);

        List<Transaction> txns = new ArrayList<>();
        txns.add(new Transaction("USR-TEST", "ACC-1", TransactionType.EXPENSE, Category.FOOD, 850.0, "2026-09-05", "Grocery"));

        budgetService.checkBudgetThresholds("USR-TEST", Category.FOOD, "2026-09-05", txns);

        assertEquals(1, capturedEvents.size());
        assertEquals(AlertEvent.EventType.BUDGET_WARNING, capturedEvents.get(0).getType());
        assertTrue(capturedEvents.get(0).getMessage().contains("85.0%"));
    }

    @Test
    @DisplayName("Observer triggers BUDGET_BREACHED event when spending exceeds 100%")
    public void testBudgetBreachObserverAlert() {
        budgetService.setBudget("USR-TEST", Category.FOOD, 1000.0, 9, 2026);

        List<Transaction> txns = new ArrayList<>();
        txns.add(new Transaction("USR-TEST", "ACC-1", TransactionType.EXPENSE, Category.FOOD, 1200.0, "2026-09-05", "Dinner"));

        budgetService.checkBudgetThresholds("USR-TEST", Category.FOOD, "2026-09-05", txns);

        assertEquals(1, capturedEvents.size());
        assertEquals(AlertEvent.EventType.BUDGET_BREACHED, capturedEvents.get(0).getType());
        assertTrue(capturedEvents.get(0).getMessage().contains("120.0%"));
    }
}
