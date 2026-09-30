package dev.xcolorful.cgccompat.shader.client.mixin.iris;

import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把 CGC 的 {@code IrisCompat} 空实现接到 Iris Shaders 上；未安装本模组时其保持原行为
 */
@Mixin(IrisCompat.class)
public class IrisCompatMixin {

    /**
     * 阴影 pass 同样会渲染实体，枪口火焰、抛壳这类内容不该进 shadow map
     */
    @Inject(method = "isRenderShadow", at = @At("HEAD"), cancellable = true)
    private static void cgcc$isRenderShadow(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ShadowRenderingState.areShadowsCurrentlyBeingRendered());
    }

    @Inject(method = "isUsingRenderPack", at = @At("HEAD"), cancellable = true)
    private static void cgcc$isUsingRenderPack(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(IrisApi.getInstance().isShaderPackInUse());
    }

    /**
     * Iris 1.9.7 起整个 {@code batchedentityrendering} 模块被删掉
     * {@code RenderBuffers.bufferSource()} 不再被换成全缓冲源
     * {@code endBatch(RenderType)} 就是真的提交，CGC 那句兜底不需要接管
     */
    @Deprecated(since = "1.21.10")
//    @Inject(method = "endBatch", at = @At("HEAD"), cancellable = true)
    private static void cgcc$endBatch(MultiBufferSource.BufferSource bufferSource, CallbackInfoReturnable<Boolean> cir) {
//        if (bufferSource instanceof FullyBufferedMultiBufferSource) {
//            bufferSource.endBatch();
//            cir.setReturnValue(true);
//        }
    }

    /**
     * Iris 的 {@code HandRenderer} 交给 {@code renderHandsWithItems} 的是空 PoseStack（单位阵基底），
     * 而不是 vanilla {@code renderItemInHand} 那种含摄像机旋转的基底，
     * 所以它渲染手部期间采到的位移是视图空间量，CGC 得自行换算。
     * <p>
     * 用 {@code isActive()} 而不是「装了光影包」来判断：没走 Iris 手部通道时（例如摄像机 detached）
     * vanilla 的 {@code renderItemInHand} 仍然会给含摄像机旋转的基底
     */
    @Inject(method = "isHandPoseStackWorldSpace", at = @At("HEAD"), cancellable = true)
    private static void cgcc$isHandPoseStackWorldSpace(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!HandRenderer.INSTANCE.isActive());
    }

    /**
     * 把 CGC 的激光束管线派给 Iris 的 program
     * <ul>
     *     <li>Iris 按 {@code RenderPipeline} <b>实例</b>查 program 表，只认识 vanilla 的管线；
     *     没派过的管线不会替换成光影包的着色器（Iris 内部还会记进 missingShaders），
     *     开光影包后光束要么完全不画、要么落进错误的 pass</li>
     *     <li>派成 {@code BEACON_BEAM}（信标光束）而不是粒子/实体：光影包里的信标光束程序必须是
     *     「不写法线 → 不进延迟光照 + 直接乘 glColor」的加性光束，与激光同构。
     *     粒子/实体程序会读 {@code Normal}（激光的顶点格式 POSITION_COLOR_TEX_LIGHTMAP 没有该属性）
     *     并套用光影包自己的粒子调色，表现为颜色被冲掉、亮度全由光影包决定</li>
     * </ul>
     */
    @Inject(method = "registerRenderPipelines", at = @At("HEAD"))
    private static void cgcc$assignLaserPipelines(CallbackInfo ci) {
        IrisApi irisApi = IrisApi.getInstance();

        irisApi.assignPipeline(ClientRenderRegistry.LaserBeamRenderState.LASER_BEAM_PIPELINE, IrisProgram.BEACON_BEAM);
        irisApi.assignPipeline(ClientRenderRegistry.LaserBeamRenderState.LASER_BEAM_ENTITY_PIPELINE, IrisProgram.EMISSIVE_ENTITIES);
    }
}
