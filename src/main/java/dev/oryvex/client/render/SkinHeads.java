package dev.oryvex.client.render;

import dev.oryvex.client.alt.SessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Async skin cache for 2D face previews. Skins are fetched by username; until (or if
 * never) downloaded, the vanilla default skin for the offline UUID is shown.
 */
public final class SkinHeads {

    private static final String SKIN_URL = "https://minotar.net/skin/";
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    private SkinHeads() {
    }

    public static ResourceLocation skin(String username) {
        String key = sanitize(username);
        ResourceLocation location = CACHE.get(key);
        if (location == null) {
            location = new ResourceLocation("oryvex", "skins/" + key);
            UUID uuid = SessionService.offlineUuid(username);
            ThreadDownloadImageData download = new ThreadDownloadImageData(null, SKIN_URL + username,
                DefaultPlayerSkin.getDefaultSkin(uuid), new ImageBufferDownload());
            Minecraft.getMinecraft().getTextureManager().loadTexture(location, download);
            CACHE.put(key, location);
        }
        return location;
    }

    public static void drawHead(String username, float x, float y, float size, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(skin(username));
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        int s = Math.round(size);
        int ix = Math.round(x);
        int iy = Math.round(y);
        Gui.drawScaledCustomSizeModalRect(ix, iy, 8.0F, 8.0F, 8, 8, s, s, 64.0F, 64.0F);
        Gui.drawScaledCustomSizeModalRect(ix, iy, 40.0F, 8.0F, 8, 8, s, s, 64.0F, 64.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static String sanitize(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toLowerCase(Locale.ROOT).toCharArray()) {
            sb.append((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' ? c : '_');
        }
        return sb.length() == 0 ? "steve" : sb.toString();
    }
}
