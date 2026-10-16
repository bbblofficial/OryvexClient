package dev.oryvex.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * UTF-8 JSON read/write with atomic replace.
 */
public final class JsonFiles {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private JsonFiles() {
    }

    public static JsonElement read(File file) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader);
        }
    }

    public static void write(File file, JsonElement element) throws IOException {
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            writer.write(GSON.toJson(element));
        }
        if (file.exists() && !file.delete()) {
            throw new IOException("Cannot replace " + file);
        }
        if (!tmp.renameTo(file)) {
            throw new IOException("Cannot move " + tmp + " to " + file);
        }
    }
}
