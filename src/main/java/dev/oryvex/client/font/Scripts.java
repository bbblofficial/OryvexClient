package dev.oryvex.client.font;

/**
 * Unicode script classification used by the font engine.
 */
public final class Scripts {

    private Scripts() {
    }

    /** Arabic-script code points (Persian, Arabic, Urdu ...) handled by Vazirmatn. */
    public static boolean isArabicScript(char c) {
        return (c >= 0x0600 && c <= 0x06FF)
            || (c >= 0x0750 && c <= 0x077F)
            || (c >= 0x08A0 && c <= 0x08FF)
            || (c >= 0xFB50 && c <= 0xFDFF)
            || (c >= 0xFE70 && c <= 0xFEFF);
    }

    /** Characters that need contextual shaping or BiDi reordering. */
    public static boolean isComplex(char c) {
        return (c >= 0x0590 && c <= 0x08FF)
            || (c >= 0xFB1D && c <= 0xFDFF)
            || (c >= 0xFE70 && c <= 0xFEFF)
            || c == 0x200F || c == 0x202B || c == 0x202E || c == 0x2067;
    }

    public static boolean containsComplex(CharSequence text) {
        if (text == null) {
            return false;
        }
        for (int i = 0, n = text.length(); i < n; i++) {
            char c = text.charAt(i);
            if (c >= 0x0590 && isComplex(c)) {
                return true;
            }
        }
        return false;
    }

    /** Whitespace and punctuation: inherit the font of their neighbours. */
    public static boolean isNeutral(char c) {
        byte dir = Character.getDirectionality(c);
        return dir == Character.DIRECTIONALITY_WHITESPACE
            || dir == Character.DIRECTIONALITY_OTHER_NEUTRALS
            || dir == Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR
            || dir == Character.DIRECTIONALITY_SEGMENT_SEPARATOR
            || dir == Character.DIRECTIONALITY_BOUNDARY_NEUTRAL;
    }

    /** Combining marks (harakat etc.) are transparent for joining purposes. */
    public static boolean isTransparent(char c) {
        int type = Character.getType(c);
        return type == Character.NON_SPACING_MARK || type == Character.ENCLOSING_MARK;
    }
}
