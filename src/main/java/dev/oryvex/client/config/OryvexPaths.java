package dev.oryvex.client.config;

import net.minecraft.client.Minecraft;

import java.io.File;

public final class OryvexPaths {

    private static File root;

    private OryvexPaths() {
    }

    public static File root() {
        if (root == null) {
            root = new File(Minecraft.getMinecraft().mcDataDir, "oryvex");
        }
        if (!root.isDirectory() && !root.mkdirs()) {
            throw new IllegalStateException("Cannot create " + root.getAbsolutePath());
        }
        return root;
    }

    public static File profiles() {
        File dir = new File(root(), "profiles");
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File file(String name) {
        return new File(root(), name);
    }
}
