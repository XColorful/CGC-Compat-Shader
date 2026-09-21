package dev.xcolorful.cgccompat.shader.forge;

import dev.xcolorful.cgccompat.shader.CgccShader;
import dev.xcolorful.cgccompat.shader.forgeclient.CgccShaderForgeClient;
import dev.xcolorful.customgun.core.api.common.McSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(CgccShader.MOD_ID)
public class CgccShaderForge {

    public CgccShaderForge() {
        Dist dist = FMLLoader.getDist();
        McSide mcSide = dist.isClient() ? McSide.CLIENT : McSide.DEDICATED_SERVER;

        CgccShader.init();

        if (mcSide == McSide.CLIENT) {
            _CgccShaderForgeClient.init();
        }
    }

    private static class _CgccShaderForgeClient {
        public static void init() {
            CgccShaderForgeClient.init();
        }
    }
}
