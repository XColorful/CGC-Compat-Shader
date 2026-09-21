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
 * 把 CGC 的 {@code IrisCompat} 空实现接到 Oculus 上；未安装本模组时其保持原行为。
 * <p>
 * 三处 {@code remap = false}：目标类在 CGC 里不参与混淆，没有映射表可查，
 * 交给 Mixin AP 去重映射会直接报 "Unable to locate obfuscation mapping"
 */
@Mixin(IrisCompat.class)
public class IrisCompatMixin {

    /**
     * 阴影 pass 同样会渲染实体，枪口火焰、抛壳这类内容不该进 shadow map
     */
    @Inject(method = "isRenderShadow", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cgcc$isRenderShadow(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ShadowRenderingState.areShadowsCurrentlyBeingRendered());
    }

    @Inject(method = "isUsingRenderPack", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cgcc$isUsingRenderPack(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(IrisApi.getInstance().isShaderPackInUse());
    }

    /**
     * 关卡渲染期间 {@code RenderBuffers.bufferSource()} 给的是全缓冲源，
     * 它把 {@code endBatch(RenderType)} 实现成了空方法，per-type 立即提交会静默失效；
     * 只有无参 {@code endBatch()} 会真正提交并清空已排队几何体
     */
    @Inject(method = "endBatch", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cgcc$endBatch(MultiBufferSource.BufferSource bufferSource, CallbackInfoReturnable<Boolean> cir) {
        if (bufferSource instanceof FullyBufferedMultiBufferSource) {
            // 刻意调在 MC 静态类型而非全缓冲源类型上：refmap 只对 MC 类生成映射，
            // 调在 Oculus 类型上会重映射落空，生产环境（SRG 名）NoSuchMethodError
            bufferSource.endBatch();
            cir.setReturnValue(true);
        }
    }
}
