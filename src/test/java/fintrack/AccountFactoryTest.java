package fintrack;

import fintrack.factory.AccountFactory;
import fintrack.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Factory Pattern and polymorphic behavior of Account subclasses.
 */
public class AccountFactoryTest {

    @Test
    @DisplayName("Factory creates SavingsAccount with minimum balance enforcement")
    public void testSavingsAccountPolymorphism() {
        Account savings = AccountFactory.createAccount(AccountType.SAVINGS, "U1", "Savings Test", 5000.0, 4.0, 1000.0);

        assertTrue(savings instanceof SavingsAccount);
        assertEquals(AccountType.SAVINGS, savings.getAccountType());
        assertEquals(4000.0, savings.getAvailableFunds()); // 5000 - 1000

        // Withdraw within bounds
        assertTrue(savings.withdraw(2000.0));
        assertEquals(3000.0, savings.getBalance());

        // Withdraw breaching minimum balance of 1000
        assertFalse(savings.withdraw(2500.0), "Should not allow withdrawal that breaches minimum balance");
        assertEquals(3000.0, savings.getBalance());

        // Interest calculation
        double interest = savings.calculateMonthlyInterest();
        assertTrue(interest > 0, "Savings should earn interest");
    }

    @Test
    @DisplayName("Factory creates CheckingAccount with overdraft protection and fees")
    public void testCheckingAccountPolymorphism() {
        Account checking = AccountFactory.createAccount(AccountType.CHECKING, "U1", "Checking Test", 2000.0, 5000.0, 10.0);

        assertTrue(checking instanceof CheckingAccount);
        assertEquals(AccountType.CHECKING, checking.getAccountType());
        assertEquals(7000.0, checking.getAvailableFunds()); // 2000 balance + 5000 overdraft

        // Normal withdraw
        assertTrue(checking.withdraw(1500.0));
        assertEquals(500.0, checking.getBalance());

        // Overdraft withdraw (dips below 0, applies 10 fee)
        assertTrue(checking.withdraw(1000.0));
        // balance was 500, withdrew 1000 + 10 fee = 1010 deduction => 500 - 1010 = -510
        assertEquals(-510.0, checking.getBalance());

        // Attempt to exceed overdraft
        assertFalse(checking.withdraw(5000.0));
    }

    @Test
    @DisplayName("Factory creates CreditAccount with credit limit and finance charges")
    public void testCreditAccountPolymorphism() {
        Account credit = AccountFactory.createAccount(AccountType.CREDIT, "U1", "Credit Card", 0.0, 50000.0, 18.0);

        assertTrue(credit instanceof CreditAccount);
        assertEquals(AccountType.CREDIT, credit.getAccountType());
        assertEquals(50000.0, credit.getAvailableFunds());

        // Charge expense (increases balance / debt)
        assertTrue(credit.withdraw(12000.0));
        assertEquals(12000.0, credit.getBalance()); // Debt owed
        assertEquals(38000.0, credit.getAvailableFunds());

        // Repay bill (decreases balance / debt)
        assertTrue(credit.deposit(5000.0));
        assertEquals(7000.0, credit.getBalance());

        // Exceed credit limit
        assertFalse(credit.withdraw(45000.0));

        // Finance charge calculation
        double charge = credit.calculateMonthlyInterest();
        assertTrue(charge > 0, "Credit balance should accrue monthly finance charges");
    }
}
