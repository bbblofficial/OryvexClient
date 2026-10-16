package dev.oryvex.client.core;

import dev.oryvex.client.OryvexClient;
import dev.oryvex.client.anim.AnimationClock;
import dev.oryvex.client.font.BiDiFontRenderer;
import dev.oryvex.client.hud.HudRenderer;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.modules.visuals.VisualsMod;
import dev.oryvex.client.ui.clickgui.OryvexClickGui;
import dev.oryvex.client.ui.screen.HudEditorScreen;
import dev.oryvex.client.util.CombatTracker;
import dev.oryvex.client.util.CpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

/**
 * Forge event bridge: frame clock, ticks, input, HUD rendering and visual hooks.
 */
public final class ClientEvents {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            AnimationClock.tick();
            RenderContext.update();
        } else {
            BiDiFontRenderer.get().collectGarbage();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ModuleManager manager = ModuleManager.getInstance();
        if (manager == null || this.mc.thePlayer == null || this.mc.theWorld == null) {
            return;
        }
        for (Module module : manager.getModules()) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (RuntimeException e) {
                    OryvexClient.LOGGER.error("Module " + module.getName() + " failed during tick", e);
                }
            }
        }
        CombatTracker.tick();
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (this.mc.currentScreen != null) {
            return;
        }
        int key = Keyboard.getEventKey();
        boolean pressed = Keyboard.getEventKeyState();
        if (OryvexClient.dashboardKey() != null && OryvexClient.dashboardKey().isPressed()) {
            this.mc.displayGuiScreen(new OryvexClickGui(null));
            return;
        }
        if (!pressed || key == Keyboard.KEY_NONE || Keyboard.isRepeatEvent()) {
            return;
        }
        ModuleManager manager = ModuleManager.getInstance();
        if (manager == null) {
            return;
        }
        for (Module module : manager.getModules()) {
            if (module.getKeybind().isBound() && module.getKeybind().getKey() == key) {
                module.toggle();
            }
            if (module.isEnabled()) {
                module.onKeyPressed(key);
            }
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (event.buttonstate && (event.button == 0 || event.button == 1)) {
            CpsTracker.click(event.button);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onOverlayPre(RenderGameOverlayEvent.Pre event) {
        VisualsMod visuals = ModuleManager.module(VisualsMod.class);
        if (visuals == null) {
            return;
        }
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            visuals.onPreHud(event.resolution);
        } else if (event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && visuals.customCrosshair()) {
            event.setCanceled(true);
            visuals.renderCrosshair(event.resolution);
        }
    }

    @SubscribeEvent
    public void onOverlayPost(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        if (this.mc.currentScreen instanceof HudEditorScreen || this.mc.gameSettings.showDebugInfo) {
            return;
        }
        HudRenderer.renderAll(event.resolution.getScaledWidth(), event.resolution.getScaledHeight(), event.partialTicks, false);
    }

    @SubscribeEvent
    public void onBlockHighlight(DrawBlockHighlightEvent event) {
        VisualsMod visuals = ModuleManager.module(VisualsMod.class);
        if (visuals != null) {
            visuals.onBlockHighlight(event);
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (event.entityPlayer == this.mc.thePlayer && event.target != null) {
            CombatTracker.onAttack(event.target);
        }
    }
}
