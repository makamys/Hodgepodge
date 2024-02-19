package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldServer.class)
public abstract class MixinWorldServerUpdateEntities extends World {

    @Shadow
    private int idleTimeout;

    /**
     * @author kubasz
     * @reason Vanilla skipping the update when no players are in the dimension causes memory leaks
     */
    @Overwrite
    public void tickEntities() {
        if (this.players.isEmpty() && getPersistentChunks().isEmpty()) {
            if (this.idleTimeout++ >= 1200) {
                // Make sure to run cleanup code every 10s
                if (this.idleTimeout % 200 == 0) {
                    super.tickEntities();
                }
                return;
            }
        } else {
            this.idleTimeout = 0;
        }

        super.tickEntities();
    }

    private MixinWorldServerUpdateEntities(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_,
            WorldSettings p_i45368_4_, Profiler p_i45368_5_) {
        // Needed because we're extending from World
        super(p_i45368_1_, p_i45368_2_, p_i45368_3_, p_i45368_4_, p_i45368_5_);
    }
}
