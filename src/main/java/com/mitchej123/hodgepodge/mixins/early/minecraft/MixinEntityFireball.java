package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityFireball.class)
public abstract class MixinEntityFireball extends Entity {

    @Shadow
    public double accelerationX;

    @Shadow
    public double accelerationY;

    @Shadow
    public double accelerationZ;

    @Inject(method = "writeCustomNbt", at = @At("TAIL"))
    public void hodgepodge$writeFireballAcceleration(NBTTagCompound tagCompound, CallbackInfo ci) {
        tagCompound.put(
                "acceleration",
                this.toNbtList(this.accelerationX, this.accelerationY, this.accelerationZ));
    }

    @Inject(method = "readCustomNbt", at = @At(value = "TAIL"))
    public void hodgepodge$readFireballAcceleration(NBTTagCompound tagCompund, CallbackInfo ci) {
        if (tagCompund.contains("acceleration", 9)) {
            NBTTagList nbttaglist = tagCompund.getList("acceleration", 6);
            this.accelerationX = nbttaglist.getDouble(0);
            this.accelerationY = nbttaglist.getDouble(1);
            this.accelerationZ = nbttaglist.getDouble(2);
        } else {
            this.remove();
        }
    }

    /* Forced to have constructor */
    private MixinEntityFireball(World worldIn) {
        super(worldIn);
    }
}
