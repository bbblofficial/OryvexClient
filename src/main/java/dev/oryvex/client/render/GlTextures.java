package dev.oryvex.client.render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * Raw OpenGL texture helpers (RGBA8, linear filtering, clamped edges).
 */
public final class GlTextures {

    private GlTextures() {
    }

    public static ByteBuffer toRgba(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = image.getRGB(0, 0, w, h, null, 0, w);
        ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);
        for (int pixel : pixels) {
            buffer.put((byte) (pixel >> 16 & 0xFF));
            buffer.put((byte) (pixel >> 8 & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) (pixel >>> 24));
        }
        buffer.flip();
        return buffer;
    }

    public static int create(int width, int height, ByteBuffer data) {
        int id = GL11.glGenTextures();
        GlStateManager.bindTexture(id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        resetUnpack();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
        return id;
    }

    public static int create(BufferedImage image) {
        return create(image.getWidth(), image.getHeight(), toRgba(image));
    }

    public static void upload(int texture, int x, int y, BufferedImage image) {
        GlStateManager.bindTexture(texture);
        resetUnpack();
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, image.getWidth(), image.getHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, toRgba(image));
    }

    public static void delete(int texture) {
        if (texture > 0) {
            GlStateManager.deleteTexture(texture);
        }
    }

    private static void resetUnpack() {
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
    }
}
