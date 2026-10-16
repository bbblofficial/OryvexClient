package dev.oryvex.client;

import dev.oryvex.client.alt.AltStore;
import dev.oryvex.client.config.ConfigManager;
import dev.oryvex.client.config.OryvexPaths;
import dev.oryvex.client.core.ClientEvents;
import dev.oryvex.client.core.ScreenRouter;
import dev.oryvex.client.font.FontRegistry;
import dev.oryvex.client.module.ModuleManager;
import dev.oryvex.client.modules.visuals.VisualsMod;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

/**
 * OryvexClient entry point (Forge mod loaded through the Mixin tweaker).
 */
@Mod(modid = OryvexClient.MODID, name = OryvexClient.NAME, version = OryvexClient.VERSION,
    acceptedMinecraftVersions = "[1.8.9]")
public final class OryvexClient {

    public static final String MODID = "oryvex";
    public static final String NAME = "OryvexClient";
    public static final String VERSION = "1.8.9";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(MODID)
    public static OryvexClient instance;

    private static ModuleManager modules;
    private static ConfigManager config;
    private static AltStore alts;
    private static KeyBinding dashboardKey;
    private static boolean shutDown;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OryvexPaths.root();
        LOGGER.info("{} {} pre-initialising in {}", NAME, VERSION, OryvexPaths.root().getAbsolutePath());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        FontRegistry.load();

        modules = new ModuleManager();
        modules.registerAll();
        config = new ConfigManager(modules);
        config.loadActive();
        alts = new AltStore();
        alts.load();

        dashboardKey = new KeyBinding("key.oryvex.dashboard", Keyboard.KEY_RSHIFT, "key.categories.oryvex");
        ClientRegistry.registerKeyBinding(dashboardKey);

        ClientEvents events = new ClientEvents();
        MinecraftForge.EVENT_BUS.register(events);
        if (FMLCommonHandler.instance().bus() != MinecraftForge.EVENT_BUS) {
            FMLCommonHandler.instance().bus().register(events);
        }
        ScreenRouter.markReady();
        LOGGER.info("{} ready: {} modules, {} saved alts, profile '{}'", NAME, modules.getModules().size(), alts.size(),
            config.getActiveProfile());
    }

    /** Called from MixinMinecraft when the game is closing. */
    public static void onShutdown() {
        if (shutDown) {
            return;
        }
        shutDown = true;
        VisualsMod visuals = ModuleManager.module(VisualsMod.class);
        if (visuals != null) {
            visuals.restoreGamma();
        }
        if (config != null) {
            config.save();
        }
        if (alts != null) {
            alts.save();
        }
    }

    public static ModuleManager modules() {
        return modules;
    }

    public static ConfigManager config() {
        return config;
    }

    public static AltStore alts() {
        return alts;
    }

    public static KeyBinding dashboardKey() {
        return dashboardKey;
    }
}
