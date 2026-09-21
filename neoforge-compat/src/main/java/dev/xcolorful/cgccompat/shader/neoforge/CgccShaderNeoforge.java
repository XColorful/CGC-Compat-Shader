package dev.xcolorful.cgccompat.shader.neoforge;

import dev.xcolorful.cgccompat.shader.CgccShader;
import dev.xcolorful.cgccompat.shader.neoforgeclient.CgccShaderNeoforgeClient;
import dev.xcolorful.customgun.core.api.common.McSide;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(CgccShader.MOD_ID)
public class CgccShaderNeoforge {

    public CgccShaderNeoforge() {
        Dist dist = FMLLoader.getDist();
        McSide mcSide = dist.isClient() ? McSide.CLIENT : McSide.DEDICATED_SERVER;

        CgccShader.init();

        if (mcSide == McSide.CLIENT) {
            _CgccShaderNeoforgeClient.init();
        }
    }

    private static class _CgccShaderNeoforgeClient {
        public static void init() {
            CgccShaderNeoforgeClient.init();
        }
    }
}