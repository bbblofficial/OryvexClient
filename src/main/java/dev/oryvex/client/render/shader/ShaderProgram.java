package dev.oryvex.client.render.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal GLSL program wrapper (OpenGL 2.0 core entry points via LWJGL 2).
 */
public final class ShaderProgram {

    private static final String ROOT = "/assets/oryvex/shaders/";

    private final String name;
    private final int program;
    private final Map<String, Integer> uniforms = new HashMap<>();

    private ShaderProgram(String name, int program) {
        this.name = name;
        this.program = program;
    }

    public static ShaderProgram load(String vertexFile, String fragmentFile) {
        int vertex = compile(GL20.GL_VERTEX_SHADER, vertexFile);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, fragmentFile);
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertex);
        GL20.glAttachShader(program, fragment);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program, 4096);
            GL20.glDeleteProgram(program);
            throw new IllegalStateException("Failed to link " + fragmentFile + ": " + log);
        }
        GL20.glDetachShader(program, vertex);
        GL20.glDetachShader(program, fragment);
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        return new ShaderProgram(fragmentFile, program);
    }

    private static int compile(int type, String file) {
        String source = readResource(ROOT + file);
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader, 4096);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("Failed to compile " + file + ": " + log);
        }
        return shader;
    }

    static String readResource(String path) {
        try (InputStream in = ShaderProgram.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing shader resource " + path);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read shader " + path, e);
        }
    }

    public void use() {
        GL20.glUseProgram(this.program);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    private int location(String uniform) {
        Integer cached = this.uniforms.get(uniform);
        if (cached == null) {
            cached = GL20.glGetUniformLocation(this.program, uniform);
            this.uniforms.put(uniform, cached);
        }
        return cached;
    }

    public ShaderProgram set1i(String uniform, int value) {
        GL20.glUniform1i(this.location(uniform), value);
        return this;
    }

    public ShaderProgram set1f(String uniform, float value) {
        GL20.glUniform1f(this.location(uniform), value);
        return this;
    }

    public ShaderProgram set2f(String uniform, float x, float y) {
        GL20.glUniform2f(this.location(uniform), x, y);
        return this;
    }

    public ShaderProgram setColor(String uniform, int argb) {
        GL20.glUniform4f(this.location(uniform),
            (argb >> 16 & 0xFF) / 255.0F,
            (argb >> 8 & 0xFF) / 255.0F,
            (argb & 0xFF) / 255.0F,
            (argb >>> 24) / 255.0F);
        return this;
    }

    public String getName() {
        return this.name;
    }
}
