package fintrack.factory;

import fintrack.model.Account;
import fintrack.model.AccountType;
import fintrack.model.CheckingAccount;
import fintrack.model.CreditAccount;
import fintrack.model.SavingsAccount;

import java.util.Map;
import java.util.UUID;

/**
 * Factory pattern implementation for instantiating polymorphic Account subtypes.
 * Decouples account creation from business logic and guarantees valid initial states.
 */
public class AccountFactory {

    /**
     * Creates an Account subclass instance based on the given AccountType and parameters.
     *
     * @param type Account type (SAVINGS, CHECKING, CREDIT)
     * @param userId Owner user ID
     * @param accountName Human-readable account label
     * @param initialBalance Initial deposit or starting balance
     * @param param1 Type-specific param 1 (Savings: interestRate, Checking: overdraftLimit, Credit: creditLimit)
     * @param param2 Type-specific param 2 (Savings: minimumBalance, Checking: transactionFee, Credit: APR)
     * @return Concrete Account subtype instance
     */
    public static Account createAccount(AccountType type, String userId, String accountName,
                                        double initialBalance, double param1, double param2) {
        String accountId = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return createAccountWithId(type, accountId, userId, accountName, initialBalance, param1, param2);
    }

    /**
     * Creates an Account subtype instance with a pre-existing account ID (used during deserialization or imports).
     */
    public static Account createAccountWithId(AccountType type, String accountId, String userId, String accountName,
                                              double initialBalance, double param1, double param2) {
        if (type == null) {
            throw new IllegalArgumentException("Account type cannot be null.");
        }

        switch (type) {
            case SAVINGS:
                // param1: interestRate (%), param2: minimumBalance
                double interestRate = (param1 >= 0) ? param1 : 4.0;
                double minBalance = (param2 >= 0) ? param2 : 1000.0;
                return new SavingsAccount(accountId, userId, accountName, initialBalance, interestRate, minBalance);

            case CHECKING:
                // param1: overdraftLimit, param2: transactionFee
                double overdraftLimit = (param1 >= 0) ? param1 : 5000.0;
                double fee = (param2 >= 0) ? param2 : 10.0;
                return new CheckingAccount(accountId, userId, accountName, initialBalance, overdraftLimit, fee);

            case CREDIT:
                // param1: creditLimit, param2: annualPercentageRate
                double creditLimit = (param1 > 0) ? param1 : 50000.0;
                double apr = (param2 >= 0) ? param2 : 18.0;
                return new CreditAccount(accountId, userId, accountName, initialBalance, creditLimit, apr);

            default:
                throw new UnsupportedOperationException("Unsupported account type: " + type);
        }
    }

    /**
     * Flexible map-based factory creation method.
     */
    public static Account createFromMap(String userId, Map<String, Object> params) {
        String typeStr = (String) params.get("accountType");
        AccountType type = AccountType.valueOf(typeStr.toUpperCase());
        String name = (String) params.get("accountName");
        double balance = ((Number) params.getOrDefault("balance", 0.0)).doubleValue();

        double param1 = ((Number) params.getOrDefault("param1", 0.0)).doubleValue();
        double param2 = ((Number) params.getOrDefault("param2", 0.0)).doubleValue();

        return createAccount(type, userId, name, balance, param1, param2);
    }
}
