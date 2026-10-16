package dev.oryvex.client.module;

public enum Category {
    HUD("HUD"),
    PVP("PvP"),
    VISUALS("Visuals"),
    MISC("Misc");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
