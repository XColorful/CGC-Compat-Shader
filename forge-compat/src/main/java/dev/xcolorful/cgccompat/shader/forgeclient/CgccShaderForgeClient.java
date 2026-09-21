package dev.xcolorful.cgccompat.shader.forgeclient;

import dev.xcolorful.cgccompat.shader.client.CgccShaderClient;

public class CgccShaderForgeClient {

    protected static boolean initialized;

    public static void init() {
        if (initialized) return;

        CgccShaderClient.init();
        initialized = true;
    }
}
