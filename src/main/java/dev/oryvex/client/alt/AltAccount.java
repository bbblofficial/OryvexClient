package dev.oryvex.client.alt;

import com.google.gson.JsonObject;

public final class AltAccount {

    private final String username;
    private boolean favorite;
    private long lastUsed;
    private final long added;

    public AltAccount(String username, boolean favorite, long lastUsed, long added) {
        this.username = username;
        this.favorite = favorite;
        this.lastUsed = lastUsed;
        this.added = added;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean isFavorite() {
        return this.favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public void markUsed() {
        this.lastUsed = System.currentTimeMillis();
    }

    public long getAdded() {
        return this.added;
    }

    public String getUuid() {
        return SessionService.offlineUuid(this.username).toString();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("username", this.username);
        json.addProperty("uuid", this.getUuid());
        json.addProperty("favorite", this.favorite);
        json.addProperty("lastUsed", this.lastUsed);
        json.addProperty("added", this.added);
        return json;
    }

    public static AltAccount fromJson(JsonObject json) {
        String name = json.get("username").getAsString();
        boolean favorite = json.has("favorite") && json.get("favorite").getAsBoolean();
        long lastUsed = json.has("lastUsed") ? json.get("lastUsed").getAsLong() : 0L;
        long added = json.has("added") ? json.get("added").getAsLong() : System.currentTimeMillis();
        return new AltAccount(name, favorite, lastUsed, added);
    }
}
