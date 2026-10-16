package dev.oryvex.client.ui.screen;

import dev.oryvex.client.OryvexClient;
import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.font.Fonts;
import dev.oryvex.client.hud.HudElement;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.RoundedRenderer;
import dev.oryvex.client.ui.clickgui.OryvexClickGui;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Magnetic drag-and-drop HUD editor with centre/edge/element snapping guides and smooth
 * wheel scaling.
 */
public final class HudEditorScreen extends OryvexScreen {

    private static final float SNAP = 4.0F;
    private static final float MARGIN = 2.0F;

    private final GuiScreen parent;
    private HudElement dragging;
    private float dragOffsetX;
    private float dragOffsetY;
    private final List<float[]> guides = new ArrayList<>();
    private final Animation guideAlpha = new Animation(Easing.EXPO_OUT, 200.0F, 0.0F);

    public HudEditorScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    protected void layout() {
        float bw = 92.0F;
        float total = bw * 3.0F + 12.0F;
        float x = (this.width - total) / 2.0F;
        float y = this.height - 32.0F;
        this.button("Mod Dashboard", OButton.Style.SECONDARY, () -> this.mc.displayGuiScreen(this.parent instanceof OryvexClickGui ? this.parent : new OryvexClickGui(this.parent)))
            .bounds(x, y, bw, 20.0F);
        this.button("Reset Layout", OButton.Style.DANGER, this::resetLayout).bounds(x + bw + 6.0F, y, bw, 20.0F);
        this.button("Done", OButton.Style.PRIMARY, () -> this.mc.displayGuiScreen(this.parent)).bounds(x + (bw + 6.0F) * 2.0F, y, bw, 20.0F);
    }

    private void resetLayout() {
        for (HudElement element : this.elements()) {
            element.resetPosition();
        }
    }

    private List<HudElement> elements() {
        List<HudElement> visible = new ArrayList<>();
        ModuleManager manager = ModuleManager.getInstance();
        if (manager != null) {
            for (HudElement element : manager.getHudElements()) {
                if (element.isVisible()) {
                    visible.add(element);
                }
            }
        }
        return visible;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float a = this.openProgress();
        RenderUtil.rect(0.0F, 0.0F, this.width, this.height, ColorUtil.mulAlpha(0x8007080A, a));
        float cx = this.width / 2.0F;
        float cy = this.height / 2.0F;
        RenderUtil.rect(cx - 0.25F, 0.0F, 0.5F, this.height, ColorUtil.mulAlpha(0x2200B4FF, a));
        RenderUtil.rect(0.0F, cy - 0.25F, this.width, 0.5F, ColorUtil.mulAlpha(0x2200B4FF, a));

        if (this.dragging != null) {
            this.drag(mouseX, mouseY);
        }

        List<HudElement> elements = this.elements();
        HudElement hovered = this.dragging != null ? this.dragging : this.elementAt(elements, mouseX, mouseY);
        for (HudElement element : elements) {
            element.draw(this.width, this.height, partialTicks, true);
            float x = element.getX(this.width, true);
            float y = element.getY(this.height, true);
            float w = element.getScaledWidth(true);
            float h = element.getScaledHeight(true);
            boolean active = element == hovered;
            RoundedRenderer.bordered(x - 2.0F, y - 2.0F, w + 4.0F, h + 4.0F, 3.0F,
                active ? 0x1A00B4FF : 0x00000000, 1.0F, active ? Theme.ACCENT_END : 0x5594A3B8);
            if (active) {
                String label = element.getDisplayName() + "  " + Math.round(element.getScale() * 100.0F) + "%";
                float lw = Fonts.width(label, 7.5F, true) + 8.0F;
                float ly = y - 15.0F < 2.0F ? y + h + 4.0F : y - 15.0F;
                RoundedRenderer.rect(x - 2.0F, ly, lw, 11.0F, 3.0F, Theme.ACCENT_START);
                Fonts.drawMiddle(label, x + 2.0F, ly + 5.5F, Theme.TEXT_PRIMARY, 7.5F, true);
            }
        }

        float ga = this.guideAlpha.animateTo(this.dragging != null && !this.guides.isEmpty() ? 1.0F : 0.0F).get();
        if (ga > 0.01F) {
            for (float[] guide : this.guides) {
                if (guide[0] == 0.0F) {
                    RenderUtil.rect(guide[1] - 0.5F, 0.0F, 1.0F, this.height, ColorUtil.mulAlpha(Theme.GLOW, ga));
                } else {
                    RenderUtil.rect(0.0F, guide[1] - 0.5F, this.width, 1.0F, ColorUtil.mulAlpha(Theme.GLOW, ga));
                }
            }
        }

        Fonts.drawCenterX("Drag to move  \u00b7  Scroll to scale  \u00b7  Right-click to reset size", this.width / 2.0F, 8.0F,
            ColorUtil.mulAlpha(Theme.TEXT_SECONDARY, a), 8.0F, false);
        this.renderButtons(mouseX, mouseY, a);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private HudElement elementAt(List<HudElement> elements, int mouseX, int mouseY) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);
            if (RenderUtil.hovered(mouseX, mouseY, element.getX(this.width, true), element.getY(this.height, true),
                element.getScaledWidth(true), element.getScaledHeight(true))) {
                return element;
            }
        }
        return null;
    }

    private void drag(int mouseX, int mouseY) {
        HudElement element = this.dragging;
        float w = element.getScaledWidth(true);
        float h = element.getScaledHeight(true);
        float x = mouseX - this.dragOffsetX;
        float y = mouseY - this.dragOffsetY;
        this.guides.clear();
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            List<float[]> xTargets = new ArrayList<>();
            List<float[]> yTargets = new ArrayList<>();
            xTargets.add(new float[]{MARGIN, MARGIN});
            xTargets.add(new float[]{this.width - MARGIN - w, this.width - MARGIN});
            xTargets.add(new float[]{this.width / 2.0F - w / 2.0F, this.width / 2.0F});
            yTargets.add(new float[]{MARGIN, MARGIN});
            yTargets.add(new float[]{this.height - MARGIN - h, this.height - MARGIN});
            yTargets.add(new float[]{this.height / 2.0F - h / 2.0F, this.height / 2.0F});
            for (HudElement other : this.elements()) {
                if (other == element) {
                    continue;
                }
                float ox = other.getX(this.width, true);
                float oy = other.getY(this.height, true);
                float ow = other.getScaledWidth(true);
                float oh = other.getScaledHeight(true);
                xTargets.add(new float[]{ox, ox});
                xTargets.add(new float[]{ox + ow - w, ox + ow});
                xTargets.add(new float[]{ox + ow + 2.0F, ox + ow + 2.0F});
                xTargets.add(new float[]{ox - w - 2.0F, ox - 2.0F});
                yTargets.add(new float[]{oy, oy});
                yTargets.add(new float[]{oy + oh - h, oy + oh});
                yTargets.add(new float[]{oy + oh + 2.0F, oy + oh + 2.0F});
                yTargets.add(new float[]{oy - h - 2.0F, oy - 2.0F});
            }
            float[] sx = closest(xTargets, x);
            if (sx != null) {
                x = sx[0];
                this.guides.add(new float[]{0.0F, sx[1]});
            }
            float[] sy = closest(yTargets, y);
            if (sy != null) {
                y = sy[0];
                this.guides.add(new float[]{1.0F, sy[1]});
            }
        }
        x = Math.max(0.0F, Math.min(this.width - w, x));
        y = Math.max(0.0F, Math.min(this.height - h, y));
        element.setPosition(x, y, this.width, this.height, true);
    }

    private static float[] closest(List<float[]> targets, float value) {
        float[] best = null;
        float bestDistance = SNAP;
        for (float[] target : targets) {
            float distance = Math.abs(target[0] - value);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = target;
            }
        }
        return best;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (OButton button : new ArrayList<>(this.buttons)) {
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        HudElement element = this.elementAt(this.elements(), mouseX, mouseY);
        if (element == null) {
            return;
        }
        if (mouseButton == 0) {
            this.dragging = element;
            this.dragOffsetX = mouseX - element.getX(this.width, true);
            this.dragOffsetY = mouseY - element.getY(this.height, true);
        } else if (mouseButton == 1) {
            element.setScale(1.0F);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.dragging = null;
        this.guides.clear();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        HudElement element = this.elementAt(this.elements(), mouseX, mouseY);
        if (element != null) {
            element.setScale(element.getScale() + (wheel > 0 ? 0.1F : -0.1F));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (OryvexClient.config() != null) {
            OryvexClient.config().save();
        }
    }
}
