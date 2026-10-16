package dev.oryvex.client.font;

import java.util.function.IntPredicate;

/**
 * Contextual Arabic/Persian letter shaping: converts logical-order base letters into their
 * Isolated / Final / Initial / Medial presentation forms (Unicode Arabic Presentation
 * Forms-A/B), including the Persian letters پ چ ژ گ ک ی and the mandatory Lam-Alef
 * ligatures. Joining honours ZWNJ (U+200C), ZWJ (U+200D), Tatweel and transparent marks.
 */
public final class ArabicShaper {

    private static final int NON_JOINING = 0;
    private static final int RIGHT_JOINING = 1;
    private static final int DUAL_JOINING = 2;
    private static final int JOIN_CAUSING = 3;
    private static final int TRANSPARENT = 4;

    private static final int ISOLATED = 0;
    private static final int FINAL = 1;
    private static final int INITIAL = 2;
    private static final int MEDIAL = 3;

    private static final char LAM = '\u0644';
    private static final char[][] FORMS = new char[0x0700 - 0x0600][];
    private static final byte[] JOINING = new byte[0x0700 - 0x0600];

    static {
        // Presentation Forms-B (U+FE70..U+FEFF)
        isolatedOnly(0x0621, 0xFE80);           // hamza
        right(0x0622, 0xFE81);                  // alef madda
        right(0x0623, 0xFE83);                  // alef hamza above
        right(0x0624, 0xFE85);                  // waw hamza
        right(0x0625, 0xFE87);                  // alef hamza below
        dual(0x0626, 0xFE89);                   // yeh hamza
        right(0x0627, 0xFE8D);                  // alef
        dual(0x0628, 0xFE8F);                   // beh
        right(0x0629, 0xFE93);                  // teh marbuta
        dual(0x062A, 0xFE95);                   // teh
        dual(0x062B, 0xFE99);                   // theh
        dual(0x062C, 0xFE9D);                   // jeem
        dual(0x062D, 0xFEA1);                   // hah
        dual(0x062E, 0xFEA5);                   // khah
        right(0x062F, 0xFEA9);                  // dal
        right(0x0630, 0xFEAB);                  // thal
        right(0x0631, 0xFEAD);                  // reh
        right(0x0632, 0xFEAF);                  // zain
        dual(0x0633, 0xFEB1);                   // seen
        dual(0x0634, 0xFEB5);                   // sheen
        dual(0x0635, 0xFEB9);                   // sad
        dual(0x0636, 0xFEBD);                   // dad
        dual(0x0637, 0xFEC1);                   // tah
        dual(0x0638, 0xFEC5);                   // zah
        dual(0x0639, 0xFEC9);                   // ain
        dual(0x063A, 0xFECD);                   // ghain
        dual(0x0641, 0xFED1);                   // feh
        dual(0x0642, 0xFED5);                   // qaf
        dual(0x0643, 0xFED9);                   // kaf (Arabic)
        dual(0x0644, 0xFEDD);                   // lam
        dual(0x0645, 0xFEE1);                   // meem
        dual(0x0646, 0xFEE5);                   // noon
        dual(0x0647, 0xFEE9);                   // heh
        right(0x0648, 0xFEED);                  // waw
        explicit(0x0649, DUAL_JOINING, 0xFEEF, 0xFEF0, 0xFBE8, 0xFBE9); // alef maksura
        dual(0x064A, 0xFEF1);                   // yeh (Arabic)

        // Presentation Forms-A (U+FB50..U+FDFF): Persian / Urdu letters
        right(0x0671, 0xFB50);                  // alef wasla
        dual(0x0679, 0xFB66);                   // tteh
        dual(0x067E, 0xFB56);                   // peh   پ
        dual(0x0686, 0xFB7A);                   // tcheh چ
        right(0x0688, 0xFB88);                  // ddal
        right(0x0691, 0xFB8C);                  // rreh
        right(0x0698, 0xFB8A);                  // jeh   ژ
        dual(0x06A4, 0xFB6A);                   // veh
        dual(0x06A9, 0xFB8E);                   // keheh ک
        dual(0x06AF, 0xFB92);                   // gaf   گ
        explicit(0x06BA, RIGHT_JOINING, 0xFB9E, 0xFB9F, 0, 0); // noon ghunna
        dual(0x06BE, 0xFBAA);                   // heh doachashmee
        right(0x06C0, 0xFBA4);                  // heh with yeh above ۀ
        dual(0x06C1, 0xFBA6);                   // heh goal
        dual(0x06CC, 0xFBFC);                   // farsi yeh ی
        right(0x06D2, 0xFBAE);                  // yeh barree

        JOINING[0x0640 - 0x0600] = JOIN_CAUSING; // tatweel
    }

    private ArabicShaper() {
    }

    private static void dual(int base, int first) {
        explicit(base, DUAL_JOINING, first, first + 1, first + 2, first + 3);
    }

    private static void right(int base, int first) {
        explicit(base, RIGHT_JOINING, first, first + 1, 0, 0);
    }

    private static void isolatedOnly(int base, int isolated) {
        explicit(base, NON_JOINING, isolated, 0, 0, 0);
    }

    private static void explicit(int base, int joining, int iso, int fin, int ini, int med) {
        FORMS[base - 0x0600] = new char[]{(char) iso, (char) fin, (char) ini, (char) med};
        JOINING[base - 0x0600] = (byte) joining;
    }

    private static int joiningType(char c) {
        if (c == '\u200D') {
            return JOIN_CAUSING;
        }
        if (c >= 0x0600 && c < 0x0700) {
            byte type = JOINING[c - 0x0600];
            if (type != NON_JOINING || FORMS[c - 0x0600] != null) {
                return type;
            }
        }
        if (Scripts.isTransparent(c)) {
            return TRANSPARENT;
        }
        return NON_JOINING;
    }

    private static boolean joinsForward(int type) {
        return type == DUAL_JOINING || type == JOIN_CAUSING;
    }

    private static boolean joinsBackward(int type) {
        return type == DUAL_JOINING || type == RIGHT_JOINING || type == JOIN_CAUSING;
    }

    private static char lamAlef(char alef) {
        switch (alef) {
            case '\u0622':
                return '\uFEF5';
            case '\u0623':
                return '\uFEF7';
            case '\u0625':
                return '\uFEF9';
            case '\u0627':
                return '\uFEFB';
            default:
                return 0;
        }
    }

    /** Shaping output with index maps between logical source text and shaped text. */
    public static final class Result {
        public final char[] chars;
        public final int length;
        /** srcToOut[i] = index in the shaped text of source char i (length = source length + 1). */
        public final int[] srcToOut;
        /** outToSrc[j] = source index of shaped char j. */
        public final int[] outToSrc;

        Result(char[] chars, int length, int[] srcToOut, int[] outToSrc) {
            this.chars = chars;
            this.length = length;
            this.srcToOut = srcToOut;
            this.outToSrc = outToSrc;
        }

        public String text() {
            return new String(this.chars, 0, this.length);
        }
    }

    public static Result shape(char[] src, int length) {
        return shape(src, length, null);
    }

    /**
     * @param canDisplay optional predicate; presentation forms the target font cannot draw
     *                   are left as base letters.
     */
    public static Result shape(char[] src, int length, IntPredicate canDisplay) {
        char[] out = new char[length];
        int[] srcToOut = new int[length + 1];
        int[] outToSrc = new int[length];
        int o = 0;
        for (int i = 0; i < length; i++) {
            char c = src[i];
            srcToOut[i] = o;
            int type = joiningType(c);
            char[] forms = c >= 0x0600 && c < 0x0700 ? FORMS[c - 0x0600] : null;
            if (forms == null) {
                out[o] = c;
                outToSrc[o] = i;
                o++;
                continue;
            }

            int prev = i - 1;
            while (prev >= 0 && joiningType(src[prev]) == TRANSPARENT) {
                prev--;
            }
            int next = i + 1;
            while (next < length && joiningType(src[next]) == TRANSPARENT) {
                next++;
            }
            boolean prevJoins = prev >= 0 && joinsForward(joiningType(src[prev]));
            boolean nextJoins = next < length && joinsBackward(joiningType(src[next]));

            if (c == LAM && i + 1 < length) {
                char ligature = lamAlef(src[i + 1]);
                if (ligature != 0) {
                    char shaped = prevJoins ? (char) (ligature + 1) : ligature;
                    if (canDisplay == null || canDisplay.test(shaped)) {
                        out[o] = shaped;
                        outToSrc[o] = i;
                        srcToOut[i + 1] = o;
                        o++;
                        i++;
                        continue;
                    }
                }
            }

            boolean canPrev = joinsBackward(type) && prevJoins;
            boolean canNext = joinsForward(type) && nextJoins;
            int form;
            if (canPrev && canNext) {
                form = MEDIAL;
            } else if (canPrev) {
                form = FINAL;
            } else if (canNext) {
                form = INITIAL;
            } else {
                form = ISOLATED;
            }
            char shaped = forms[form];
            if (shaped == 0 && form == MEDIAL) {
                shaped = forms[FINAL];
            }
            if (shaped == 0 && form == INITIAL) {
                shaped = forms[ISOLATED];
            }
            if (shaped == 0 || (canDisplay != null && !canDisplay.test(shaped))) {
                shaped = c;
            }
            out[o] = shaped;
            outToSrc[o] = i;
            o++;
        }
        srcToOut[length] = o;
        return new Result(out, o, srcToOut, outToSrc);
    }

    public static String shape(String text) {
        char[] chars = text.toCharArray();
        return shape(chars, chars.length).text();
    }
}
