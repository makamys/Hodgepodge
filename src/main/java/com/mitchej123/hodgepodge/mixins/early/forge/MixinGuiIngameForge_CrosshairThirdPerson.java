package com.mitchej123.hodgepodge.mixins.early.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.GuiIngameForge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameForge.class)
public class MixinGuiIngameForge_CrosshairThirdPerson extends GuiIngame {

    @Inject(method = "renderCrosshairs", at = @At("HEAD"), cancellable = true, remap = false)
    public void hodgepodge$hideCrosshairThirdPerson(int width, int height, CallbackInfo ci) {
        if (minecraft.options.perspective != 0) {
            ci.cancel();
        }
    }

    private MixinGuiIngameForge_CrosshairThirdPerson(Minecraft mc) {
        super(mc);
    }
}
