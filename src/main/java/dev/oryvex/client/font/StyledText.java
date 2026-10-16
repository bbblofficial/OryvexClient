package dev.oryvex.client.font;

/**
 * Parses Minecraft formatting codes (section sign + code) into plain characters with
 * per-character color and style, mirroring vanilla FontRenderer semantics.
 */
public final class StyledText {

    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINE = 4;
    public static final int STRIKE = 8;
    public static final int OBFUSCATED = 16;

    private static final String CODES = "0123456789abcdefklmnor";
    private static final int[] COLOR_CODES = new int[32];

    static {
        for (int i = 0; i < 32; i++) {
            int j = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + j;
            int g = (i >> 1 & 1) * 170 + j;
            int b = (i & 1) * 170 + j;
            if (i == 6) {
                r += 85;
            }
            if (i >= 16) {
                r /= 4;
                g /= 4;
                b /= 4;
            }
            COLOR_CODES[i] = (r & 255) << 16 | (g & 255) << 8 | b & 255;
        }
    }

    public final char[] chars;
    public final int length;
    public final int[] rgb;
    public final byte[] style;

    private StyledText(char[] chars, int length, int[] rgb, byte[] style) {
        this.chars = chars;
        this.length = length;
        this.rgb = rgb;
        this.style = style;
    }

    public static StyledText parse(String text, int baseRgb, boolean shadow, boolean baseBold) {
        int n = text.length();
        char[] chars = new char[n];
        int[] rgb = new int[n];
        byte[] style = new byte[n];
        int baseStyle = baseBold ? BOLD : 0;
        int currentRgb = baseRgb;
        int currentStyle = baseStyle;
        int len = 0;
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            if (c == '\u00a7' && i + 1 < n) {
                int code = CODES.indexOf(Character.toLowerCase(text.charAt(i + 1)));
                if (code < 16) {
                    currentStyle = baseStyle;
                    if (code < 0) {
                        code = 15;
                    }
                    currentRgb = COLOR_CODES[shadow ? code + 16 : code];
                } else if (code == 16) {
                    currentStyle |= OBFUSCATED;
                } else if (code == 17) {
                    currentStyle |= BOLD;
                } else if (code == 18) {
                    currentStyle |= STRIKE;
                } else if (code == 19) {
                    currentStyle |= UNDERLINE;
                } else if (code == 20) {
                    currentStyle |= ITALIC;
                } else {
                    currentStyle = baseStyle;
                    currentRgb = baseRgb;
                }
                i++;
                continue;
            }
            chars[len] = c;
            rgb[len] = currentRgb;
            style[len] = (byte) currentStyle;
            len++;
        }
        return new StyledText(chars, len, rgb, style);
    }

    public boolean isComplex() {
        for (int i = 0; i < this.length; i++) {
            char c = this.chars[i];
            if (c >= 0x0590 && Scripts.isComplex(c)) {
                return true;
            }
        }
        return false;
    }

    public String plain() {
        return new String(this.chars, 0, this.length);
    }

    public static String strip(String text) {
        return parse(text, 0, false, false).plain();
    }
}
