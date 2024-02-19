package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.util.ChunkCoordinates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkCoordinates.class)
public class MixinChunkCoordinates {

    @Shadow
    public int x;

    @Shadow
    public int y;

    @Shadow
    public int z;

    /**
     * @author mitchej123
     * @reason Swap out the default (terrible) hashCode function with a better one
     */
    @Overwrite
    public int hashCode() {
        return this.x * 8976890 + this.y * 981131 + this.z;
    }
}
