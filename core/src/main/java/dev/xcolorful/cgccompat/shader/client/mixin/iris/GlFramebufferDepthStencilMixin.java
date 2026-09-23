package dev.xcolorful.cgccompat.shader.client.mixin.iris;

import com.mojang.renderpearl.api.textures.GpuTexture;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让 Iris 挂 depth 附件时把 stencil 方面一并暴露出来
 * <ul>
 *     <li>Iris 的 {@code RenderTargets} 复用的是主 render target 的那张 depth 纹理（{@code IrisRenderingPipeline} 取 {@code MainTarget.getDepthTexture()} 传进去）</li>
 *     <li>而 CGC 已经让主 target 用上 combined depth-stencil 格式（{@code ConfigureMainRenderTargetEvent::enableStencil}）</li>
 *     <li>但 {@code addDepthAttachment} 用的是 {@code GL_DEPTH_ATTACHMENT}：combined 格式的纹理挂上去会退化成「只有 depth 方面」，
 *     FBO 上查 {@code GL_STENCIL_ATTACHMENT} 得到 {@code GL_NONE}（实测 fbo=77）</li>
 * </ul>
 * 这一条决定了光影下瞄具能不能用模板测试
 * <ul>
 *     <li>开光影时 Iris 会在 {@code FullyBufferedMultiBufferSource.endBatch()} 期间跳过 pass 的 FBO 绑定（{@code MixinGlCommandEncoder} 对 {@code _glBindFramebuffer} 的 redirect），
 *     于是 CGC 的瞄具几何体实际画进 Iris 当时已绑定的世界 FBO，而不是主 target</li>
 *     <li>那个 FBO 的 depth 附件就是这张纹理：不暴露 stencil → 模板测试无从比较 → 目镜遮罩、准心恒真，模板写入也不生效</li>
 *     <li>改成 {@code GL_DEPTH_STENCIL_ATTACHMENT} 之后，世界 FBO 与主 target 共用同一张纹理、stencil 缓冲也随之共用：
 *     经 Iris 缓冲源画的遮罩/准心/模板写入，与走 vanilla 即时路径画的圆形模板孔（{@code RenderType#debugTriangleFan().draw}）、
 *     以及 {@code _clearStencilBuffer} 清的，都是同一个模板缓冲</li>
 * </ul>
 * 只对带 stencil 方面的纹理生效，depth-only（{@code DEPTH32}，例如阴影视差用的）保持原样
 */
@Mixin(GlFramebuffer.class)
public class GlFramebufferDepthStencilMixin {

    /**
     * {@code addDepthAttachment} 的入参只在 redirect 里拿不到，先暂存一份
     * （渲染线程单线程调用，不需要同步）
     */
    @Unique
    private GpuTexture cgcc$attachingDepthTexture;

    @Inject(method = "addDepthAttachment(Lcom/mojang/renderpearl/api/textures/GpuTexture;)V", at = @At("HEAD"))
    private void cgcc$captureDepthTexture(GpuTexture depthTexture, CallbackInfo ci) {
        this.cgcc$attachingDepthTexture = depthTexture;
    }

    @Redirect(
            method = "addDepthAttachment(Lcom/mojang/renderpearl/api/textures/GpuTexture;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/IrisRenderSystem;framebufferTexture2D(IIIIII)V"
            )
    )
    private void cgcc$attachDepthStencil(int fbo, int target, int attachment, int textureTarget, int texture, int level) {
        GpuTexture depthTexture = this.cgcc$attachingDepthTexture;

        int actualAttachment = attachment == GL30.GL_DEPTH_ATTACHMENT
                && depthTexture != null
                && depthTexture.getFormat().hasStencilAspect()
                ? GL30.GL_DEPTH_STENCIL_ATTACHMENT
                : attachment;

        IrisRenderSystem.framebufferTexture2D(fbo, target, actualAttachment, textureTarget, texture, level);
    }
}
