package dev.xcolorful.cgccompat.shader.client;

public class CgccShaderClient {

    protected static boolean initialized;

    public static void init() {
        if (initialized) return;

        initialized = true;
    }
}
