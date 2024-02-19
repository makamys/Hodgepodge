package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

/**
 * This mixin is a backport of a Forge fix https://github.com/MinecraftForge/MinecraftForge/pull/4729
 */
@Mixin(World.class)
public abstract class MixinWorld_FixLightUpdateLag {

    @Shadow
    public abstract boolean isAreaLoaded(int p_72873_1_, int p_72873_2_, int p_72873_3_, int p_72873_4_);

    @ModifyConstant(method = "checkLight", constant = @Constant(intValue = 17, ordinal = 0))
    public int hodgepodge$modifyRangeCheck1(int cst) {
        return 16;
    }

    @Inject(
            method = "checkLight",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/World;profiler:Lnet/minecraft/profiler/Profiler;",
                    shift = At.Shift.BEFORE,
                    ordinal = 0))
    public void hodgepodge$modifyUpdateRange(EnumSkyBlock p_147463_1_, int x, int y, int z,
            CallbackInfoReturnable<Boolean> cir, @Share("updateRange") LocalIntRef updateRange) {
        updateRange.set(this.isAreaLoaded(x, y, z, 18) ? 17 : 15);
    }

    @ModifyConstant(
            method = "checkLight",
            constant = { @Constant(intValue = 17, ordinal = 1), @Constant(intValue = 17, ordinal = 2) })
    public int hodgepodge$modifyRangeCheck2(int cst, @Share("updateRange") LocalIntRef updateRange) {
        return updateRange.get();
    }
}
