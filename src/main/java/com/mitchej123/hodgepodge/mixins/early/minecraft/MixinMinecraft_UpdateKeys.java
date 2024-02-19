package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mitchej123.hodgepodge.mixins.interfaces.KeyBindingExt;

@Mixin(Minecraft.class)
public class MixinMinecraft_UpdateKeys {

    /**
     * From Sk1er/Patcher
     */
    @Inject(
            method = "closeScreen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MouseHelper;lock()V"))
    private void hodgepodge$updateKeysStates(CallbackInfo ci) {
        ((KeyBindingExt) Minecraft.getInstance().options.attackKey).hodgepodge$updateKeyStates();
    }

}
