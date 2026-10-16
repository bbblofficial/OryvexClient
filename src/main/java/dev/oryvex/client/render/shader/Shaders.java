package dev.oryvex.client.render.shader;

import dev.oryvex.client.OryvexClient;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

/**
 * Lazily compiled shader registry. If anything fails (old GPU, driver bug) the UI falls
 * back to flat rectangles instead of crashing.
 */
public final class Shaders {

    private static boolean initialised;
    private static boolean available;
    private static ShaderProgram rect;
    private static ShaderProgram blurDown;
    private static ShaderProgram blurUp;
    private static ShaderProgram blurMask;
    private static ShaderProgram nebula;

    private Shaders() {
    }

    private static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        try {
            ContextCapabilities caps = GLContext.getCapabilities();
            if (!OpenGlHelper.shadersSupported || caps == null || !caps.OpenGL20) {
                OryvexClient.LOGGER.warn("GLSL shaders unavailable; using flat UI rendering");
                return;
            }
            rect = ShaderProgram.load("position.vsh", "rounded_rect.fsh");
            blurDown = ShaderProgram.load("position.vsh", "kawase_down.fsh");
            blurUp = ShaderProgram.load("position.vsh", "kawase_up.fsh");
            blurMask = ShaderProgram.load("position.vsh", "blur_mask.fsh");
            nebula = ShaderProgram.load("position.vsh", "nebula.fsh");
            available = true;
        } catch (Throwable t) {
            OryvexClient.LOGGER.error("Failed to initialise Oryvex shaders, falling back to flat UI", t);
            available = false;
        }
    }

    public static boolean available() {
        init();
        return available;
    }

    public static ShaderProgram rect() {
        return available() ? rect : null;
    }

    public static ShaderProgram blurDown() {
        return available() ? blurDown : null;
    }

    public static ShaderProgram blurUp() {
        return available() ? blurUp : null;
    }

    public static ShaderProgram blurMask() {
        return available() ? blurMask : null;
    }

    public static ShaderProgram nebula() {
        return available() ? nebula : null;
    }
}
