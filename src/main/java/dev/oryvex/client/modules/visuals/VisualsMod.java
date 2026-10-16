package dev.oryvex.client.modules.visuals;

import dev.oryvex.client.module.Category;
import dev.oryvex.client.module.Module;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.render.ColorUtil;
import dev.oryvex.client.render.MotionBlurRenderer;
import dev.oryvex.client.render.RenderUtil;
import dev.oryvex.client.setting.BooleanSetting;
import dev.oryvex.client.setting.ColorSetting;
import dev.oryvex.client.setting.ModeSetting;
import dev.oryvex.client.setting.NumberSetting;
import dev.oryvex.client.ui.theme.Theme;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import org.lwjgl.opengl.GL11;

public final class VisualsMod extends Module {

    private static final float FULLBRIGHT_GAMMA = 16.0F;

    private final BooleanSetting fullbright = this.add(new BooleanSetting("Fullbright", false));
    private final BooleanSetting crosshair = this.add(new BooleanSetting("Custom Crosshair", true));
    private final ModeSetting crosshairStyle = this.add(new ModeSetting("Crosshair Style", "Cross", "Cross", "Dot", "Cross + Dot", "Square"))
        .visibleWhen(() -> this.crosshair.isOn());
    private final NumberSetting crosshairSize = this.add(new NumberSetting("Crosshair Size", 5.0D, 1.0D, 12.0D, 0.5D))
        .visibleWhen(() -> this.crosshair.isOn());
    private final NumberSetting crosshairGap = this.add(new NumberSetting("Crosshair Gap", 2.0D, 0.0D, 8.0D, 0.5D))
        .visibleWhen(() -> this.crosshair.isOn());
    private final NumberSetting crosshairThickness = this.add(new NumberSetting("Crosshair Thickness", 1.0D, 0.5D, 4.0D, 0.5D))
        .visibleWhen(() -> this.crosshair.isOn());
    private final BooleanSetting crosshairOutline = this.add(new BooleanSetting("Crosshair Outline", true))
        .visibleWhen(() -> this.crosshair.isOn());
    private final ColorSetting crosshairColor = this.add(new ColorSetting("Crosshair Color", Theme.GLOW))
        .visibleWhen(() -> this.crosshair.isOn());
    private final BooleanSetting blockOverlay = this.add(new BooleanSetting("Block Overlay", true));
    private final ColorSetting overlayOutline = this.add(new ColorSetting("Overlay Outline", Theme.ACCENT_END))
        .visibleWhen(() -> this.blockOverlay.isOn());
    private final ColorSetting overlayFill = this.add(new ColorSetting("Overlay Fill", 0x2E007BFF))
        .visibleWhen(() -> this.blockOverlay.isOn());
    private final NumberSetting overlayWidth = this.add(new NumberSetting("Overlay Line Width", 2.5D, 1.0D, 6.0D, 0.5D))
        .visibleWhen(() -> this.blockOverlay.isOn());
    private final BooleanSetting hitColor = this.add(new BooleanSetting("Hit Color", true));
    private final ColorSetting hitColorValue = this.add(new ColorSetting("Hit Tint", 0x6600B4FF))
        .visibleWhen(() -> this.hitColor.isOn());
    private final BooleanSetting motionBlur = this.add(new BooleanSetting("Motion Blur", false));
    private final NumberSetting motionBlurAmount = this.add(new NumberSetting("Motion Blur Amount", 5.0D, 1.0D, 9.0D, 1.0D))
        .visibleWhen(() -> this.motionBlur.isOn());

    private float savedGamma = -1.0F;

    public VisualsMod() {
        super("Visuals", "جلوه\u200cهای بصری", "Fullbright, custom crosshair, electric-blue block overlay, hit color and motion blur.",
            Category.VISUALS, true);
    }

    private static VisualsMod active() {
        VisualsMod mod = ModuleManager.module(VisualsMod.class);
        return mod != null && mod.isEnabled() ? mod : null;
    }

    /** ARGB hit tint, or 0 when vanilla red should be used. */
    public static int hitTint() {
        VisualsMod mod = active();
        return mod != null && mod.hitColor.isOn() ? mod.hitColorValue.getColor() : 0;
    }

    @Override
    public void onTick() {
        if (this.fullbright.isOn()) {
            if (this.mc.gameSettings.gammaSetting != FULLBRIGHT_GAMMA) {
                if (this.savedGamma < 0.0F) {
                    this.savedGamma = this.mc.gameSettings.gammaSetting > 1.0F ? 1.0F : this.mc.gameSettings.gammaSetting;
                }
                this.mc.gameSettings.gammaSetting = FULLBRIGHT_GAMMA;
            }
        } else {
            this.restoreGamma();
        }
    }

    @Override
    protected void onDisable() {
        this.restoreGamma();
        MotionBlurRenderer.reset();
    }

    public void restoreGamma() {
        if (this.savedGamma >= 0.0F && this.mc.gameSettings != null) {
            this.mc.gameSettings.gammaSetting = this.savedGamma;
            this.savedGamma = -1.0F;
        } else if (this.mc.gameSettings != null && this.mc.gameSettings.gammaSetting > 1.0F && !this.fullbright.isOn()) {
            this.mc.gameSettings.gammaSetting = 1.0F;
        }
    }

    public boolean customCrosshair() {
        return this.isEnabled() && this.crosshair.isOn();
    }

    /** Runs before the HUD: world is fully rendered into the main framebuffer. */
    public void onPreHud(ScaledResolution sr) {
        if (this.isEnabled() && this.motionBlur.isOn() && this.mc.theWorld != null) {
            MotionBlurRenderer.apply(this.motionBlurAmount.getFloat() / 10.0F, sr);
        } else {
            MotionBlurRenderer.reset();
        }
    }

    public void renderCrosshair(ScaledResolution sr) {
        if (this.mc.gameSettings.thirdPersonView != 0) {
            return;
        }
        float cx = sr.getScaledWidth() / 2.0F;
        float cy = sr.getScaledHeight() / 2.0F;
        float size = this.crosshairSize.getFloat();
        float gap = this.crosshairGap.getFloat();
        float t = this.crosshairThickness.getFloat();
        int color = this.crosshairColor.getColor();
        boolean outline = this.crosshairOutline.isOn();
        String style = this.crosshairStyle.get();
        if (style.startsWith("Cross")) {
            this.bar(cx - t / 2.0F, cy - gap - size, t, size, color, outline);
            this.bar(cx - t / 2.0F, cy + gap, t, size, color, outline);
            this.bar(cx - gap - size, cy - t / 2.0F, size, t, color, outline);
            this.bar(cx + gap, cy - t / 2.0F, size, t, color, outline);
        }
        if (style.equals("Dot") || style.equals("Cross + Dot")) {
            float d = Math.max(1.0F, t * 1.5F);
            this.bar(cx - d / 2.0F, cy - d / 2.0F, d, d, color, outline);
        }
        if (style.equals("Square")) {
            float s = size + gap;
            if (outline) {
                RenderUtil.outline(cx - s - 0.5F, cy - s - 0.5F, s * 2.0F + 1.0F, s * 2.0F + 1.0F, t + 1.0F, 0xC0000000);
            }
            RenderUtil.outline(cx - s, cy - s, s * 2.0F, s * 2.0F, t, color);
        }
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void bar(float x, float y, float w, float h, int color, boolean outline) {
        if (outline) {
            RenderUtil.rect(x - 0.5F, y - 0.5F, w + 1.0F, h + 1.0F, 0xC0000000);
        }
        RenderUtil.rect(x, y, w, h, color);
    }

    public void onBlockHighlight(DrawBlockHighlightEvent event) {
        if (!this.isEnabled() || !this.blockOverlay.isOn()) {
            return;
        }
        MovingObjectPosition target = event.target;
        if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        EntityPlayer player = event.player;
        World world = player.worldObj;
        BlockPos pos = target.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        if (block.getMaterial() == Material.air || !world.getWorldBorder().contains(pos)) {
            return;
        }
        event.setCanceled(true);
        float pt = event.partialTicks;
        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * pt;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * pt;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * pt;
        block.setBlockBoundsBasedOnState(world, pos);
        AxisAlignedBB box = block.getSelectedBoundingBox(world, pos).expand(0.002D, 0.002D, 0.002D).offset(-dx, -dy, -dz);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();

        RenderUtil.color(this.overlayFill.getColor());
        fillBox(box);
        GL11.glLineWidth(this.overlayWidth.getFloat());
        RenderUtil.color(this.overlayOutline.getColor());
        outlineBox(box);
        GL11.glLineWidth(1.0F);

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void fillBox(AxisAlignedBB b) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        // bottom
        wr.pos(b.minX, b.minY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.maxZ).endVertex();
        wr.pos(b.minX, b.minY, b.maxZ).endVertex();
        // top
        wr.pos(b.minX, b.maxY, b.minZ).endVertex();
        wr.pos(b.minX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.minZ).endVertex();
        // north
        wr.pos(b.minX, b.minY, b.minZ).endVertex();
        wr.pos(b.minX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.minZ).endVertex();
        // south
        wr.pos(b.minX, b.minY, b.maxZ).endVertex();
        wr.pos(b.maxX, b.minY, b.maxZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.minX, b.maxY, b.maxZ).endVertex();
        // west
        wr.pos(b.minX, b.minY, b.minZ).endVertex();
        wr.pos(b.minX, b.minY, b.maxZ).endVertex();
        wr.pos(b.minX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.minX, b.maxY, b.minZ).endVertex();
        // east
        wr.pos(b.maxX, b.minY, b.minZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.maxX, b.minY, b.maxZ).endVertex();
        tessellator.draw();
    }

    private static void outlineBox(AxisAlignedBB b) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        wr.pos(b.minX, b.minY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.maxZ).endVertex();
        wr.pos(b.minX, b.minY, b.maxZ).endVertex();
        wr.pos(b.minX, b.minY, b.minZ).endVertex();
        tessellator.draw();
        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        wr.pos(b.minX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.minX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.minX, b.maxY, b.minZ).endVertex();
        tessellator.draw();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        wr.pos(b.minX, b.minY, b.minZ).endVertex();
        wr.pos(b.minX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.minZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.maxZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.minX, b.minY, b.maxZ).endVertex();
        wr.pos(b.minX, b.maxY, b.maxZ).endVertex();
        tessellator.draw();
    }

    /** Converts an ARGB tint into the 4 floats expected by the entity brightness buffer. */
    public static float[] tintComponents(int argb) {
        return new float[]{
            ColorUtil.red(argb) / 255.0F, ColorUtil.green(argb) / 255.0F,
            ColorUtil.blue(argb) / 255.0F, ColorUtil.alpha(argb) / 255.0F
        };
    }
}
