package fintrack.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fintrack.model.Account;
import fintrack.model.Budget;
import fintrack.model.Transaction;
import fintrack.model.User;
import fintrack.util.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Local JSON file persistence store using Google Gson.
 * Features atomic writes and thread-safe data access.
 */
public class JsonFileStore {

    private final String dataFilePath;
    private final Gson gson;
    private final Object lock = new Object();

    public static class DataContainer {
        public List<User> users = new ArrayList<>();
        public List<Account> accounts = new ArrayList<>();
        public List<Transaction> transactions = new ArrayList<>();
        public List<Budget> budgets = new ArrayList<>();
    }

    public JsonFileStore() {
        this("data/fintrack_data.json");
    }

    public JsonFileStore(String dataFilePath) {
        this.dataFilePath = dataFilePath;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Account.class, new AccountAdapter())
                .setPrettyPrinting()
                .create();
        initStorage();
    }

    private void initStorage() {
        try {
            Path path = Paths.get(dataFilePath);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                saveData(new DataContainer());
                Logger.getInstance().info("Initialized fresh JSON datastore at " + dataFilePath);
            }
        } catch (IOException e) {
            Logger.getInstance().error("Failed to initialize data store", e);
        }
    }

    public DataContainer loadData() {
        synchronized (lock) {
            Path path = Paths.get(dataFilePath);
            if (!Files.exists(path)) {
                return new DataContainer();
            }

            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                DataContainer data = gson.fromJson(reader, DataContainer.class);
                if (data == null) {
                    data = new DataContainer();
                }
                if (data.users == null) data.users = new ArrayList<>();
                if (data.accounts == null) data.accounts = new ArrayList<>();
                if (data.transactions == null) data.transactions = new ArrayList<>();
                if (data.budgets == null) data.budgets = new ArrayList<>();
                return data;
            } catch (Exception e) {
                Logger.getInstance().error("Failed to read JSON data from " + dataFilePath, e);
                return new DataContainer();
            }
        }
    }

    public void saveData(DataContainer data) {
        synchronized (lock) {
            Path targetPath = Paths.get(dataFilePath);
            Path tempPath = Paths.get(dataFilePath + ".tmp");

            try {
                if (targetPath.getParent() != null && !Files.exists(targetPath.getParent())) {
                    Files.createDirectories(targetPath.getParent());
                }

                try (Writer writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
                    gson.toJson(data, writer);
                }

                // Atomic replacement of target file
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Logger.getInstance().error("Atomic write failed for " + dataFilePath, e);
                // Fallback direct write
                try (Writer writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8)) {
                    gson.toJson(data, writer);
                } catch (IOException ex) {
                    Logger.getInstance().error("Critical persistence failure writing to " + dataFilePath, ex);
                }
            }
        }
    }
}
