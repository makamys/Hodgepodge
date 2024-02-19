package com.mitchej123.hodgepodge.mixins.early.minecraft;

import java.util.Random;

import net.minecraft.block.BlockGrass;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockGrass.class)
public class MixinBlockGrass {

    /**
     * @author tth05
     * @reason Small performance improvements. Prevent chunk loading, avoid doing any work on blocks that can't turn to
     *         grass, remove duplicate {@link World#getRawBrightness(int, int, int)} call.
     */
    @Overwrite
    public void tick(World worldIn, int x, int y, int z, Random random) {
        if (worldIn.isMultiplayer) return;

        int blockLightValue = worldIn.getRawBrightness(x, y + 1, z);
        if (blockLightValue < 4 && worldIn.getBlockLightOpacity(x, y + 1, z) > 2) {
            worldIn.setBlock(x, y, z, Blocks.DIRT);
        } else if (blockLightValue >= 9) {
            for (int i = 0; i < 4; ++i) {
                int targetX = x + random.nextInt(3) - 1;
                int targetY = y + random.nextInt(5) - 3;
                int targetZ = z + random.nextInt(3) - 1;

                if (targetX == x && targetZ == z && (targetY == y || targetY == y - 1)) continue;
                if (!worldIn.isChunkLoaded(targetX, targetY, targetZ)) continue;

                if (worldIn.getBlock(targetX, targetY, targetZ) == Blocks.DIRT
                        && worldIn.getBlockMetadata(targetX, targetY, targetZ) == 0
                        && worldIn.getRawBrightness(targetX, targetY + 1, targetZ) >= 4
                        && worldIn.getBlockLightOpacity(targetX, targetY + 1, targetZ) <= 2) {
                    worldIn.setBlock(targetX, targetY, targetZ, Blocks.GRASS);
                }
            }
        }
    }
}
