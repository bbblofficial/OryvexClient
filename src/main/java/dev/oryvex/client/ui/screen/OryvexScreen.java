package dev.oryvex.client.ui.screen;

import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.modules.misc.InterfaceMod;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.render.blur.KawaseBlur;
import dev.oryvex.client.ui.theme.Theme;
import dev.oryvex.client.ui.widget.OButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base screen: open animation, Oryvex buttons and glass helpers.
 */
public abstract class OryvexScreen extends GuiScreen {

    protected final List<OButton> buttons = new ArrayList<>();
    protected final Animation open = new Animation(Easing.SMOOTH, 520.0F, 0.0F);

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttons.clear();
        this.open.animateTo(1.0F);
        this.layout();
    }

    /** Creates and positions widgets. Called on open and every resize. */
    protected abstract void layout();

    protected OButton button(String label, OButton.Style style, Runnable action) {
        OButton button = new OButton(label, style, action);
        this.buttons.add(button);
        return button;
    }

    protected float openProgress() {
        return this.open.get();
    }

    protected void renderButtons(int mouseX, int mouseY, float alpha) {
        for (OButton button : this.buttons) {
            button.render(mouseX, mouseY, alpha);
        }
    }

    /** Blurred, tinted glass frame with the neon-slate border. */
    protected void glass(float x, float y, float w, float h, float radius, float alpha) {
        dev.oryvex.client.render.RoundedRenderer.bordered(x, y, w, h, radius, ColorUtil.mulAlpha(Theme.SLATE, alpha), 1.0F,
            ColorUtil.mulAlpha(Theme.BORDER, alpha));
    }

    /** Blurs the live world behind the whole screen. */
    protected void worldBackdrop(float alpha) {
        RenderUtil.rect(0.0F, 0.0F, this.width, this.height, ColorUtil.mulAlpha(Theme.OBSIDIAN, 0.65F * alpha));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (OButton button : new ArrayList<>(this.buttons)) {
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
