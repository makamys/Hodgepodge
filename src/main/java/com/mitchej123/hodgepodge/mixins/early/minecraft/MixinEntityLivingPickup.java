package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.entity.EntityLiving;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityLiving.class)
public class MixinEntityLivingPickup {

    @Shadow
    private boolean canPickupLoot;

    /**
     * @author mitchej123
     * @reason Stop monsters from picking up loot and not despawning
     */
    @Overwrite
    public void setCanPickupLoot(boolean canPickUpLoot) {
        this.canPickupLoot = false;
    }

    /**
     * @author mitchej123
     * @reason Stop monsters from picking up loot and not despawning
     */
    @Overwrite
    public boolean canPickupLoot() {
        return false;
    }
}
