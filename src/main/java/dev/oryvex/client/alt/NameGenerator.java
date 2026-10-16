package dev.oryvex.client.alt;

import java.util.Random;

/**
 * Random PvP-flavoured usernames that always satisfy the 3-16 [A-Za-z0-9_] rule.
 */
public final class NameGenerator {

    private static final String[] PREFIXES = {
        "Void", "Neon", "Frost", "Shadow", "Blaze", "Crit", "Combo", "Sweat", "Night", "Volt",
        "Hyper", "Toxic", "Nova", "Pixel", "Rapid", "Ghost", "Storm", "Apex", "Zero", "Strafe",
        "Cyan", "Onyx", "Obsidian", "Rogue", "Flux", "Glitch", "Turbo", "Sonic", "Echo", "Drift"
    };
    private static final String[] SUFFIXES = {
        "Hitz", "Clicker", "PvP", "Tap", "Strafer", "God", "Bridger", "Ace", "Reach", "Main",
        "Sniper", "Wolf", "Fang", "Byte", "Rush", "Edge", "Kai", "Rex", "Blade", "Pot",
        "Jitter", "Butterfly", "Wtap", "Sumo", "Duels", "Bow", "Rod", "Combo", "Lord", "X"
    };
    private static final Random RANDOM = new Random();

    private NameGenerator() {
    }

    public static String generate() {
        for (int attempt = 0; attempt < 32; attempt++) {
            String prefix = PREFIXES[RANDOM.nextInt(PREFIXES.length)];
            String suffix = SUFFIXES[RANDOM.nextInt(SUFFIXES.length)];
            StringBuilder name = new StringBuilder();
            switch (RANDOM.nextInt(4)) {
                case 0:
                    name.append(prefix).append(suffix);
                    break;
                case 1:
                    name.append(prefix).append('_').append(suffix);
                    break;
                case 2:
                    name.append(prefix).append(suffix).append(RANDOM.nextInt(100));
                    break;
                default:
                    name.append(prefix.toLowerCase()).append(suffix).append(1 + RANDOM.nextInt(9));
                    break;
            }
            String candidate = name.toString();
            if (SessionService.isValidName(candidate)) {
                return candidate;
            }
        }
        return "Oryvex" + (1000 + RANDOM.nextInt(9000));
    }
}
