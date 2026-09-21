package dev.xcolorful.cgccompat.shader.neoforgeclient;

import dev.xcolorful.cgccompat.shader.client.CgccShaderClient;

public class CgccShaderNeoforgeClient {

    protected static boolean initialized;

    public static void init() {
        if (initialized) return;

        CgccShaderClient.init();

        initialized = true;
    }
}
