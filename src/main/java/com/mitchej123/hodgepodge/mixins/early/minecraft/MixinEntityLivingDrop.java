package com.mitchej123.hodgepodge.mixins.early.minecraft;

import static org.objectweb.asm.Opcodes.PUTFIELD;

import net.minecraft.entity.EntityLiving;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLiving.class)
public class MixinEntityLivingDrop {

    @Shadow
    private boolean persistent;

    @Shadow
    protected void dropEquipment(boolean wasHitByPlayer, int lootMultiplicatorPct) {
        throw new AbstractMethodError("Shadow");
    }

    /**
     * @author ElNounch
     * @reason Don't disable despawning for loot picking reasons
     */
    @Redirect(
            method = "tickAi()V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/EntityLiving;persistent:Z",
                    opcode = PUTFIELD))
    public void hodgepodge$dontSetPersistenceRequired(EntityLiving o, boolean newVal) {}

    /**
     * @author ElNounch
     * @reason Drop picked up items when despawning
     */
    @Inject(
            method = "checkDespawn()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;remove()V"))
    public void hodgepodge$despawnEntity_dropPickedLoot(CallbackInfo ci) {
        this.dropEquipment(false, 0);
    }
}
