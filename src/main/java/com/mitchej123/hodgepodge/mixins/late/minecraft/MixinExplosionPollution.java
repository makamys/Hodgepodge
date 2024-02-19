package com.mitchej123.hodgepodge.mixins.late.minecraft;

import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mitchej123.hodgepodge.config.PollutionConfig;
import com.mitchej123.hodgepodge.util.PollutionHelper;

/*
 * Merged from ModMixins under the MIT License Copyright bartimaeusnek & GTNewHorizons
 */
@Mixin(Explosion.class)
public class MixinExplosionPollution {

    @Shadow
    float power;

    @Shadow
    World world;

    @Shadow
    double x;

    @Shadow
    double z;

    @Inject(method = "damageEntities", at = @At(value = "TAIL"))
    public void hodgepodge$addExplosionPollution(CallbackInfo ci) {
        if (!this.world.isMultiplayer) PollutionHelper.addPollution(
                this.world.getChunk((int) this.x, (int) this.z),
                (int) Math.ceil(power * PollutionConfig.explosionPollutionAmount));
    }
}
