package com.mitchej123.hodgepodge.mixins.early.minecraft;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase_FixPotionException extends Entity {

    @Shadow
    @Final
    private HashMap<Integer, PotionEffect> statusEffects;

    @Shadow
    private boolean effectsChanged;

    private MixinEntityLivingBase_FixPotionException(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    /**
     * @author laetansky
     * @reason Fix {@link ConcurrentModificationException} being thrown when modifying active potions inside those forge
     *         event handlers which could be fired while iterating over active potion effect. Fix is back ported from
     *         newer versions.
     */
    @Overwrite
    protected void tickStatusEffects() {
        Iterator<Integer> iterator = this.statusEffects.keySet().iterator();

        try {
            while (iterator.hasNext()) {
                Integer integer = iterator.next();
                PotionEffect potioneffect = this.statusEffects.get(integer);

                if (!potioneffect.tick(((EntityLivingBase) (Object) this))) {
                    if (!this.world.isMultiplayer) {
                        iterator.remove();
                        this.onStatusEffectRemoved(potioneffect);
                    }
                } else if (potioneffect.getDuration() % 600 == 0) {
                    this.onStatusEffectUpgraded(potioneffect, false);
                }
            }
        } catch (ConcurrentModificationException ignored) {}

        int i;

        if (this.effectsChanged) {
            if (!this.world.isMultiplayer) {
                if (this.statusEffects.isEmpty()) {
                    this.dataTracker.update(8, (byte) 0);
                    this.dataTracker.update(7, 0);
                    this.setInvisible(false);
                } else {
                    i = PotionHelper.getColor(this.statusEffects.values());
                    this.dataTracker.update(
                            8,
                            (byte) (PotionHelper.isAllAmbient(this.statusEffects.values()) ? 1 : 0));
                    this.dataTracker.update(7, i);
                    this.setInvisible(this.hasStatusEffect(Potion.INVISIBILITY.id));
                }
            }

            this.effectsChanged = false;
        }

        i = this.dataTracker.getInt(7);
        boolean flag1 = this.dataTracker.getByte(8) > 0;

        if (i > 0) {
            boolean flag;

            if (!this.isInvisible()) {
                flag = this.random.nextBoolean();
            } else {
                flag = this.random.nextInt(15) == 0;
            }

            if (flag1) {
                flag &= this.random.nextInt(5) == 0;
            }

            if (flag) {
                double d0 = (double) (i >> 16 & 255) / 255.0D;
                double d1 = (double) (i >> 8 & 255) / 255.0D;
                double d2 = (double) (i >> 0 & 255) / 255.0D;
                this.world.addParticle(
                        flag1 ? "mobSpellAmbient" : "mobSpell",
                        this.x + (this.random.nextDouble() - 0.5D) * (double) this.width,
                        this.y + this.random.nextDouble() * (double) this.height - (double) this.eyeHeight,
                        this.z + (this.random.nextDouble() - 0.5D) * (double) this.width,
                        d0,
                        d1,
                        d2);
            }
        }
    }

    @Shadow
    protected void onStatusEffectRemoved(PotionEffect p_70688_1_) {}

    @Shadow
    protected void onStatusEffectUpgraded(PotionEffect p_70695_1_, boolean p_70695_2_) {}

    @Shadow
    public abstract boolean hasStatusEffect(int p_82165_1_);
}
