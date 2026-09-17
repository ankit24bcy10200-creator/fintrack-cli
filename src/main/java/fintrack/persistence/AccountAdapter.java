package fintrack.persistence;

import com.google.gson.*;
import fintrack.model.*;

import java.lang.reflect.Type;

/**
 * Custom Gson TypeAdapter for polymorphic serialization and deserialization of the abstract Account class.
 * Demonstrates Abstraction and Polymorphism persistence handling.
 */
public class AccountAdapter implements JsonSerializer<Account>, JsonDeserializer<Account> {

    @Override
    public JsonElement serialize(Account src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = context.serialize(src, src.getClass()).getAsJsonObject();
        result.addProperty("accountType", src.getAccountType().name());
        return result;
    }

    @Override
    public Account deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement typeElem = jsonObject.get("accountType");

        if (typeElem == null) {
            throw new JsonParseException("Missing 'accountType' field in Account JSON");
        }

        String typeName = typeElem.getAsString();
        AccountType type = AccountType.valueOf(typeName.toUpperCase());

        switch (type) {
            case SAVINGS:
                return context.deserialize(jsonObject, SavingsAccount.class);
            case CHECKING:
                return context.deserialize(jsonObject, CheckingAccount.class);
            case CREDIT:
                return context.deserialize(jsonObject, CreditAccount.class);
            default:
                throw new JsonParseException("Unknown account type: " + typeName);
        }
    }
}
