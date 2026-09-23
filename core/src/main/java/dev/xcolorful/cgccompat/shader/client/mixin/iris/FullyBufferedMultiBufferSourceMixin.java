package dev.xcolorful.cgccompat.shader.client.mixin.iris;

import org.jetbrains.annotations.ApiStatus;

/**
 * 让 Iris 的全缓冲源在 flush 时把 NeoForge 的模板测试带上
 * <ul>
 *     <li>1.21.6 的模板测试（{@code RenderSystem.STENCIL_TEST}）在 vanilla 侧只有 {@code RenderType.CompositeRenderType#draw} 会读它</li>
 *     <li>而装了 Iris 后 {@code RenderBuffers.bufferSource()} 被换成本类，它的 flush 是自己建 {@code RenderPass} 再 {@code drawIndexed}，整条链没有 {@code enableStencilTest}</li>
 *     <li>于是 CGC 瞄具的模板遮罩（目镜黑遮罩 {@code GL_EQUAL}、准心 {@code GL_EQUAL}、镜身 {@code GL_EQUAL}）一律退化成恒真，圆孔写入也不生效</li>
 *     <li>表现为不装兼容模组也复现的：准心始终可见、倍镜 ocular 是模型自身的黑色（开不开光影包都一样）</li>
 * </ul>
 * 这里按 vanilla 的写法补回同一行，使延迟 flush 与即时绘制语义一致
 * <ul>
 *     <li>只读 ambient 状态，{@code RenderSystem.STENCIL_TEST} 为 null（模板窗口之外）时不做任何事</li>
 *     <li>光影包开启时 pass 的深度纹理没有 stencil 方面（Iris 会把命中材质映射的几何体重定向到自己的 framebuffer），
 *     GL 规范下没有 stencil 缓冲就等同于恒真，所以那一侧本改动不改变表现</li>
 * </ul>
 */
@Deprecated(since = "1.21.10")
@ApiStatus.AvailableSince("1.21.6")
//@Mixin(FullyBufferedMultiBufferSource.class)
public class FullyBufferedMultiBufferSourceMixin {

//    @WrapOperation(
//            method = {"endBatch", "endBatchWithType"},
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lcom/mojang/blaze3d/systems/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalInt;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;"
//            )
//    )
//    private RenderPass cgcc$applyStencilTest(CommandEncoder encoder,
//                                             Supplier<String> debugGroup,
//                                             GpuTextureView colorTexture,
//                                             OptionalInt clearColor,
//                                             GpuTextureView depthTexture,
//                                             OptionalDouble clearDepth,
//                                             Operation<RenderPass> original) {
//        RenderPass pass = original.call(encoder, debugGroup, colorTexture, clearColor, depthTexture, clearDepth);
//
//        var stencilTest = RenderSystem.STENCIL_TEST; // neoforge类
//        if (stencilTest != null) {
//            pass.enableStencilTest(stencilTest);
//        }
//
//        return pass;
//    }
}
