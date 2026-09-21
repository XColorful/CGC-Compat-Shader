package dev.xcolorful.cgccompat.shader.client.mixin.iris;

import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
     * 关卡渲染期间 {@code RenderBuffers.bufferSource()} 给的是全缓冲源，
     * 它把 {@code endBatch(RenderType)} 实现成了空方法，per-type 立即提交会静默失效；
     * 只有无参 {@code endBatch()} 会真正提交并清空已排队几何体
     */
    @Inject(method = "endBatch", at = @At("HEAD"), cancellable = true)
    private static void cgcc$endBatch(MultiBufferSource.BufferSource bufferSource, CallbackInfoReturnable<Boolean> cir) {
        if (bufferSource instanceof FullyBufferedMultiBufferSource) {
            bufferSource.endBatch();
            cir.setReturnValue(true);
        }
    }
}
