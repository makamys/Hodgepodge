package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.mitchej123.hodgepodge.Common;

@Mixin(RandomPositionGenerator.class)
public class MixinRandomPositionGenerator {

    /**
     * @author mitchej123
     * @reason Backported fix north/west bias
     */
    @Overwrite
    private static Vec3 getTarget(EntityCreature entityCreature, int hor, int ver, Vec3 facing) {
        boolean found = false;
        double tx = 0, ty = 0, tz = 0;
        float bestValue = -99999.0F;
        boolean tooFar = false;

        if (entityCreature.inVillage()) {
            final double d0 = entityCreature.getPos().squaredDistanceTo(
                    MathHelper.floor(entityCreature.x),
                    MathHelper.floor(entityCreature.y),
                    MathHelper.floor(entityCreature.z)) + 4.0F;
            final double d1 = entityCreature.getVillageRadius() /* getMaximumHomeDistance() */ + (double) hor;
            tooFar = d0 < d1 * d1;
        }

        for (int i = 0; i < 10; ++i) {
            final int x1 = Common.RNG.nextInt(2 * hor + 1) - hor;
            final int y1 = Common.RNG.nextInt(2 * ver + 1) - ver;
            final int z1 = Common.RNG.nextInt(2 * hor + 1) - hor;

            if (facing == null || (double) x1 * facing.x + (double) z1 * facing.z >= 0.0D) {
                // Use the rounded coordinates for comparision since `isWithinHomeDistance` takes int params
                final int x2 = x1 + MathHelper.floor(entityCreature.x);
                final int y2 = y1 + MathHelper.floor(entityCreature.y);
                final int z2 = z1 + MathHelper.floor(entityCreature.z);

                if (!tooFar || entityCreature.isValidGoalTarget(x2, y2, z2)) {
                    final float blockPathWeight = entityCreature.getPathfindingFavor(x2, y2, z2);

                    if (blockPathWeight > bestValue) {
                        bestValue = blockPathWeight;
                        // But use the un-rounded coordinates for moving (so we avoid chopping off fractional
                        // coordinates and biasing to the NW)
                        tx = entityCreature.x + x1;
                        ty = entityCreature.y + y1;
                        tz = entityCreature.z + z1;
                        found = true;
                        if (blockPathWeight == 0.0F) {
                            // Don't keep searching if the black path weight function isn't implemented
                            break;
                        }
                    }
                }
            }
        }

        return found ? Vec3.of(tx, ty, tz) : null;
    }
}
