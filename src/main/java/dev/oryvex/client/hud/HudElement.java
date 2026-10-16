package dev.oryvex.client.hud;

import com.google.gson.JsonObject;
import dev.oryvex.client.anim.Animation;
import dev.oryvex.client.anim.Easing;
import dev.oryvex.client.module.Module;
import net.minecraft.client.renderer.GlStateManager;

import java.util.function.BooleanSupplier;

/**
 * A draggable, scalable HUD widget. Position is stored as a fraction of the free screen
 * space so layouts survive resolution and GUI-scale changes.
 */
public abstract class HudElement {

    public static final float MIN_SCALE = 0.5F;
    public static final float MAX_SCALE = 2.5F;

    private final Module owner;
    private final String id;
    private final String displayName;
    private final float defaultX;
    private final float defaultY;
    private BooleanSupplier condition = () -> true;
    private float relX;
    private float relY;
    private float scale = 1.0F;
    private final Animation scaleAnimation = new Animation(Easing.EXPO_OUT, 220.0F, 1.0F);

    protected HudElement(Module owner, String id, String displayName, float defaultX, float defaultY) {
        this.owner = owner;
        this.id = id;
        this.displayName = displayName;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.relX = defaultX;
        this.relY = defaultY;
    }

    public HudElement showWhen(BooleanSupplier condition) {
        this.condition = condition;
        return this;
    }

    /** Unscaled width in GUI units. */
    public abstract float getWidth(boolean preview);

    /** Unscaled height in GUI units. */
    public abstract float getHeight(boolean preview);

    /** Renders the widget with its top-left corner at the origin (unscaled space). */
    protected abstract void render(float partialTicks, boolean preview);

    public boolean isVisible() {
        return this.owner.isEnabled() && this.condition.getAsBoolean();
    }

    public void draw(float screenWidth, float screenHeight, float partialTicks, boolean preview) {
        float s = this.getAnimatedScale();
        float x = this.getX(screenWidth, preview);
        float y = this.getY(screenHeight, preview);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(s, s, 1.0F);
        this.render(partialTicks, preview);
        GlStateManager.popMatrix();
    }

    public float getScaledWidth(boolean preview) {
        return this.getWidth(preview) * this.getAnimatedScale();
    }

    public float getScaledHeight(boolean preview) {
        return this.getHeight(preview) * this.getAnimatedScale();
    }

    public float getX(float screenWidth, boolean preview) {
        return this.relX * Math.max(0.0F, screenWidth - this.getScaledWidth(preview));
    }

    public float getY(float screenHeight, boolean preview) {
        return this.relY * Math.max(0.0F, screenHeight - this.getScaledHeight(preview));
    }

    public void setPosition(float x, float y, float screenWidth, float screenHeight, boolean preview) {
        float freeX = Math.max(1.0F, screenWidth - this.getScaledWidth(preview));
        float freeY = Math.max(1.0F, screenHeight - this.getScaledHeight(preview));
        this.relX = Math.max(0.0F, Math.min(1.0F, x / freeX));
        this.relY = Math.max(0.0F, Math.min(1.0F, y / freeY));
    }

    public float getScale() {
        return this.scale;
    }

    public float getAnimatedScale() {
        return this.scaleAnimation.get();
    }

    public void setScale(float scale) {
        this.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.round(scale * 20.0F) / 20.0F));
        this.scaleAnimation.animateTo(this.scale);
    }

    public void resetPosition() {
        this.relX = this.defaultX;
        this.relY = this.defaultY;
        this.scale = 1.0F;
        this.scaleAnimation.set(1.0F);
    }

    public Module getOwner() {
        return this.owner;
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("x", this.relX);
        json.addProperty("y", this.relY);
        json.addProperty("scale", this.scale);
        return json;
    }

    public void fromJson(JsonObject json) {
        if (json.has("x")) {
            this.relX = Math.max(0.0F, Math.min(1.0F, json.get("x").getAsFloat()));
        }
        if (json.has("y")) {
            this.relY = Math.max(0.0F, Math.min(1.0F, json.get("y").getAsFloat()));
        }
        if (json.has("scale")) {
            this.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, json.get("scale").getAsFloat()));
            this.scaleAnimation.set(this.scale);
        }
    }
}
