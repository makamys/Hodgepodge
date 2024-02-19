package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.client.gui.GuiGameOver;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGameOver.class)
public class MixinGuiGameOver {

    // Number of ticks screen was open
    @Shadow
    private int ticksSinceDeath;

    /**
     * @author ElNounch
     * @reason Fix Game Over GUI buttons disabled if switching fullscreen
     */
    @Inject(method = "init", at = @At("HEAD"))
    public void hodgepodge$resetedInitGui(CallbackInfo ci) {
        if (this.ticksSinceDeath > 19) {
            // Make sure buttons will be re-enabled next tick
            this.ticksSinceDeath = 19;
        }
    }
}
