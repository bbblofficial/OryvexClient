package dev.oryvex.client.ui.theme;

/**
 * Oryvex visual identity: deep black surfaces with an muted blue accent.
 * All colors are ARGB.
 */
public final class Theme {

    /** Pure Obsidian Black #07080A at 90% alpha. */
    public static final int OBSIDIAN = 0xE607080A;
    public static final int OBSIDIAN_SOLID = 0xFF07080A;
    /** Deep Slate #0E1117 at 85% alpha. */
    public static final int SLATE = 0xD90E1117;
    public static final int SLATE_SOLID = 0xFF0E1117;
    /** Slightly lifted slate for hover states. */
    public static final int SLATE_RAISED = 0xE6151B26;
    /** 1px slate border #1C2433. */
    public static final int BORDER = 0xFF1C2433;

    /** Flat muted blue accent. */
    public static final int ACCENT_START = 0xFF4C78A8;
    public static final int ACCENT_END = 0xFF6B8FB8;
    /** Secondary muted blue accent. */
    public static final int GLOW = 0xFF8AA4BF;

    public static final int TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFF94A3B8;
    public static final int TEXT_MUTED = 0xFF475569;

    public static final int DANGER = 0xFFFF4D6A;
    public static final int SUCCESS = 0xFF22D3A6;
    public static final int FAVORITE = 0xFFFFC447;

    public static final float RADIUS_PANEL = 8.0F;
    public static final float RADIUS_CARD = 6.0F;
    public static final float RADIUS_BUTTON = 5.0F;

    private Theme() {
    }
}
