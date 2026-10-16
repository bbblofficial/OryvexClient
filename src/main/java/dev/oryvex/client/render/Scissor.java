package dev.oryvex.client.render;

import dev.oryvex.client.core.RenderContext;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Nested scissor stack in GUI coordinates (intersection of every pushed rectangle).
 */
public final class Scissor {

    private static final Deque<int[]> STACK = new ArrayDeque<>();

    private Scissor() {
    }

    public static void push(float x, float y, float w, float h) {
        Minecraft mc = Minecraft.getMinecraft();
        int scale = RenderContext.scaleFactor();
        int px = (int) Math.floor(x * scale);
        int py = (int) Math.floor(mc.displayHeight - (y + h) * scale);
        int pw = (int) Math.ceil(w * scale);
        int ph = (int) Math.ceil(h * scale);
        int[] rect = {px, py, Math.max(0, pw), Math.max(0, ph)};
        int[] parent = STACK.peek();
        if (parent != null) {
            int x1 = Math.max(rect[0], parent[0]);
            int y1 = Math.max(rect[1], parent[1]);
            int x2 = Math.min(rect[0] + rect[2], parent[0] + parent[2]);
            int y2 = Math.min(rect[1] + rect[3], parent[1] + parent[3]);
            rect = new int[]{x1, y1, Math.max(0, x2 - x1), Math.max(0, y2 - y1)};
        }
        STACK.push(rect);
        apply(rect);
    }

    public static void pop() {
        STACK.poll();
        int[] parent = STACK.peek();
        if (parent == null) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            apply(parent);
        }
    }

    private static void apply(int[] rect) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(rect[0], rect[1], rect[2], rect[3]);
    }
}
