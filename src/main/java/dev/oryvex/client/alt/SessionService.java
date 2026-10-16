package dev.oryvex.client.alt;

import dev.oryvex.client.bridge.IMixinMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Offline ("cracked") session switching at runtime. Offline sessions only authenticate on
 * servers running with online-mode=false (LAN worlds, offline/test servers).
 */
public final class SessionService {

    private static final Pattern VALID_NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    private SessionService() {
    }

    public static boolean isValidName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }

    /** Deterministic offline UUID, identical to the one vanilla servers compute. */
    public static UUID offlineUuid(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    public static Session createOffline(String username) {
        if (!isValidName(username)) {
            throw new IllegalArgumentException("Username must be 3-16 characters: letters, digits or _");
        }
        return new Session(username, offlineUuid(username).toString(), "0", "legacy");
    }

    /** Swaps Minecraft's session in place without restarting. */
    public static void login(String username) {
        Session session = createOffline(username);
        ((IMixinMinecraft) Minecraft.getMinecraft()).oryvex$setSession(session);
    }

    public static boolean isOffline(Session session) {
        String token = session.getToken();
        return token == null || token.isEmpty() || "0".equals(token) || "FML".equals(token) || "-".equals(token);
    }
}
