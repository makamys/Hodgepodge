package com.mitchej123.hodgepodge.mixins.early.minecraft;

import java.util.Map;
import java.util.UUID;

import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraftforge.common.UsernameCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

@Mixin(NetHandlerLoginServer.class)
public abstract class MixinNetHandlerLoginServer_OfflineMode {

    @Shadow
    private GameProfile profile;

    @Shadow
    public abstract void disconnect(String reason);

    /**
     * @author Caedis
     * @reason Allows a server to run offline but only allow players who have logged in while it was online (and they
     *         get their UUID so stuff works as expected).
     */
    @Overwrite
    protected GameProfile func_152506_a(GameProfile original) {
        Map<UUID, String> usernameCache = UsernameCache.getMap();
        for (Map.Entry<UUID, String> entry : usernameCache.entrySet()) {
            if (entry.getValue().equals(original.getName())) {
                return new GameProfile(entry.getKey(), original.getName());
            }
        }
        return null;
    }

    @Inject(
            method = "acceptLogin()V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/server/network/NetHandlerLoginServer;func_152506_a(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/GameProfile;",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void hodgepodge$func_147326_c(CallbackInfo ci) {
        if (this.profile == null) {
            // Disconnect null profiles from func_152506_a
            this.disconnect(
                    "Login while the server is in online mode to be able to login while it is in offline mode.");
            ci.cancel();
        }
    }
}
