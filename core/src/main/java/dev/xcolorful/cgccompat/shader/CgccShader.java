package dev.xcolorful.cgccompat.shader;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class CgccShader {
    public static final String MOD_ID = "cgccshader";
    public static final Logger LOGGER = LogUtils.getLogger();

    protected static boolean initialized;

    public static void init() {
        if (initialized) return;

        initialized = true;
    }
}
