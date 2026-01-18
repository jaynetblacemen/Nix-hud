package me.Azz_9.flex_hud.client.mixin;

import me.Azz_9.flex_hud.client.configurableModules.modules.notHud.MotionBlur;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void onRenderWorldEnd(RenderTickCounter tickCounter, CallbackInfo ci) {
        MotionBlur.onWorldRenderEnd(tickCounter.getTickProgress(false));
    }
}
