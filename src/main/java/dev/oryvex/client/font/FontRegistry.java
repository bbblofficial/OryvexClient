package dev.oryvex.client.font;

import dev.oryvex.client.OryvexClient;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads Inter (Latin) and Vazirmatn (Persian/Arabic) from the jar and resolves the font
 * family for every code point (dual-font dynamic fallback).
 */
public final class FontRegistry {

    public static final int INTER = 0;
    public static final int VAZIR = 1;
    public static final int FALLBACK = 2;
    private static final int FAMILIES = 3;

    private static final String ROOT = "/assets/oryvex/fonts/";
    private static final float MEASURE_SIZE = 64.0F;

    public static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private static final Font[] REGULAR = new Font[FAMILIES];
    private static final Font[] BOLD = new Font[FAMILIES];
    private static final Map<Long, Font> DERIVED = new HashMap<>();
    private static final float[][] ADVANCES = new float[FAMILIES * 2][];
    private static final byte[] FAMILY_CACHE = new byte[65536];
    private static volatile boolean loaded;

    private FontRegistry() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        Font dialog = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
        Font interRegular = tryLoad("Inter-Regular.ttf");
        Font interBold = tryLoad("Inter-Bold.ttf");
        Font vazirRegular = tryLoad("Vazirmatn-Regular.ttf");
        Font vazirBold = tryLoad("Vazirmatn-Bold.ttf");

        REGULAR[INTER] = interRegular != null ? interRegular : dialog;
        BOLD[INTER] = interBold != null ? interBold : REGULAR[INTER].deriveFont(Font.BOLD);
        REGULAR[VAZIR] = vazirRegular != null ? vazirRegular : dialog;
        BOLD[VAZIR] = vazirBold != null ? vazirBold : REGULAR[VAZIR].deriveFont(Font.BOLD);
        REGULAR[FALLBACK] = dialog;
        BOLD[FALLBACK] = dialog.deriveFont(Font.BOLD);

        Arrays.fill(FAMILY_CACHE, (byte) -1);
        loaded = true;
        OryvexClient.LOGGER.info("Font engine ready (Inter: {}, Vazirmatn: {})",
            interRegular != null ? "bundled" : "fallback", vazirRegular != null ? "bundled" : "fallback");
    }

    private static Font tryLoad(String file) {
        try (InputStream raw = FontRegistry.class.getResourceAsStream(ROOT + file)) {
            if (raw == null) {
                OryvexClient.LOGGER.warn("Font {} not found in jar, using SansSerif fallback", file);
                return null;
            }
            return Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(raw));
        } catch (Exception e) {
            OryvexClient.LOGGER.warn("Could not load font " + file, e);
            return null;
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static Font base(int family, boolean bold) {
        return bold ? BOLD[family] : REGULAR[family];
    }

    /** Cached derived font at a pixel size. */
    public static Font derive(int family, boolean bold, boolean italic, float size) {
        long key = ((long) Float.floatToIntBits(size) << 8) | (family << 2) | (bold ? 2 : 0) | (italic ? 1 : 0);
        synchronized (DERIVED) {
            Font font = DERIVED.get(key);
            if (font == null) {
                Font base = base(family, bold);
                font = base.deriveFont(base.getStyle() | (italic ? Font.ITALIC : 0), size);
                DERIVED.put(key, font);
            }
            return font;
        }
    }

    /** Selects the family that renders {@code c}: Vazirmatn for Arabic script, Inter for Latin. */
    public static int familyFor(char c) {
        byte cached = FAMILY_CACHE[c];
        if (cached >= 0) {
            return cached;
        }
        int family;
        if (Scripts.isArabicScript(c)) {
            family = REGULAR[VAZIR].canDisplay(c) ? VAZIR : FALLBACK;
        } else if (REGULAR[INTER].canDisplay(c)) {
            family = INTER;
        } else if (REGULAR[VAZIR].canDisplay(c)) {
            family = VAZIR;
        } else {
            family = FALLBACK;
        }
        FAMILY_CACHE[c] = (byte) family;
        return family;
    }

    public static boolean canDisplay(int family, char c) {
        return REGULAR[family].canDisplay(c);
    }

    /** Horizontal advance of {@code c} in em units (multiply by the font size). */
    public static float advanceEm(char c, int family, boolean bold) {
        int index = family * 2 + (bold ? 1 : 0);
        float[] table = ADVANCES[index];
        if (table == null) {
            table = new float[65536];
            Arrays.fill(table, Float.NaN);
            ADVANCES[index] = table;
        }
        float value = table[c];
        if (Float.isNaN(value)) {
            Font font = derive(family, bold, false, MEASURE_SIZE);
            GlyphVector gv = font.createGlyphVector(FRC, new char[]{c});
            value = gv.getGlyphMetrics(0).getAdvance() / MEASURE_SIZE;
            table[c] = value;
        }
        return value;
    }
}
