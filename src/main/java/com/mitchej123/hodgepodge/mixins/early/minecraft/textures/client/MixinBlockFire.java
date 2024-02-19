package com.mitchej123.hodgepodge.mixins.early.minecraft.textures.client;

import net.minecraft.block.BlockFire;
import net.minecraft.util.IIcon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mitchej123.hodgepodge.textures.AnimationsRenderUtils;

@Mixin(BlockFire.class)
public class MixinBlockFire {

    @Shadow
    private IIcon[] fireSprite;

    @Inject(method = "get", at = @At("HEAD"))
    private void hodgepodge$markFireAnimationForUpdate(int p_149840_1_, CallbackInfoReturnable<IIcon> cir) {
        AnimationsRenderUtils.markBlockTextureForUpdate(fireSprite[p_149840_1_]);
    }
}
