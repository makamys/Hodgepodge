package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.command.CommandTime;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;

/*
 * Merged from ModMixins under the MIT License Copyright bartimaeusnek & GTNewHorizons
 */
@Mixin(CommandTime.class)
public class MixinTimeCommandGalacticraftFix {

    @Inject(method = "setTimeOfDay", at = @At("HEAD"), cancellable = true)
    protected final void hodgepodge$setTime(ICommandSender p_71552_1_, int p_71552_2_, CallbackInfo x) {
        for (WorldServer server : MinecraftServer.getInstance().worlds) {
            if (server.dimension instanceof WorldProviderSpace) {
                ((WorldProviderSpace) server.dimension).setWorldTimeCommand(p_71552_2_);
            } else {
                server.setTimeOfDay(p_71552_2_);
            }
        }

        x.cancel();
    }

    @Inject(method = "addToTimeOfDay", at = @At("HEAD"), cancellable = true)
    protected final void hodgepodge$addTime(ICommandSender p_71553_1_, int p_71553_2_, CallbackInfo x) {
        for (WorldServer server : MinecraftServer.getInstance().worlds) {
            if (server.dimension instanceof WorldProviderSpace) {
                final WorldProviderSpace provider = (WorldProviderSpace) server.dimension;
                provider.setWorldTimeCommand(provider.getWorldTimeCommand() + p_71553_2_);
            } else {
                server.setTimeOfDay(server.getTimeOfDay() + p_71553_2_);
            }
        }

        x.cancel();
    }
}
