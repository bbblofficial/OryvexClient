package dev.oryvex.client.alt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.oryvex.client.OryvexClient;
import dev.oryvex.client.config.JsonFiles;
import dev.oryvex.client.config.OryvexPaths;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Persistent offline account list stored in oryvex/alts.json.
 */
public final class AltStore {

    private final List<AltAccount> accounts = new ArrayList<>();

    private File file() {
        return OryvexPaths.file("alts.json");
    }

    public void load() {
        this.accounts.clear();
        File file = this.file();
        if (!file.isFile()) {
            return;
        }
        try {
            JsonElement root = JsonFiles.read(file);
            JsonArray array = root.isJsonObject() && root.getAsJsonObject().has("accounts")
                ? root.getAsJsonObject().getAsJsonArray("accounts")
                : root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();
            for (JsonElement element : array) {
                if (element.isJsonObject() && element.getAsJsonObject().has("username")) {
                    AltAccount account = AltAccount.fromJson(element.getAsJsonObject());
                    if (SessionService.isValidName(account.getUsername()) && this.find(account.getUsername()) == null) {
                        this.accounts.add(account);
                    }
                }
            }
        } catch (Exception e) {
            OryvexClient.LOGGER.error("Could not read alts.json", e);
        }
    }

    public void save() {
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        JsonArray array = new JsonArray();
        for (AltAccount account : this.accounts) {
            array.add(account.toJson());
        }
        root.add("accounts", array);
        try {
            JsonFiles.write(this.file(), root);
        } catch (IOException e) {
            OryvexClient.LOGGER.error("Could not save alts.json", e);
        }
    }

    public AltAccount find(String username) {
        for (AltAccount account : this.accounts) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return account;
            }
        }
        return null;
    }

    public AltAccount add(String username) {
        AltAccount existing = this.find(username);
        if (existing != null) {
            return existing;
        }
        AltAccount account = new AltAccount(username, false, 0L, System.currentTimeMillis());
        this.accounts.add(account);
        this.save();
        return account;
    }

    public void remove(AltAccount account) {
        this.accounts.remove(account);
        this.save();
    }

    public void toggleFavorite(AltAccount account) {
        account.setFavorite(!account.isFavorite());
        this.save();
    }

    public void markUsed(AltAccount account) {
        account.markUsed();
        this.save();
    }

    /** Favorites first, then most recently used, then newest. */
    public List<AltAccount> sorted() {
        List<AltAccount> list = new ArrayList<>(this.accounts);
        list.sort(Comparator.comparing(AltAccount::isFavorite).reversed()
            .thenComparing(Comparator.comparingLong(AltAccount::getLastUsed).reversed())
            .thenComparing(Comparator.comparingLong(AltAccount::getAdded).reversed()));
        return list;
    }

    public int size() {
        return this.accounts.size();
    }
}
