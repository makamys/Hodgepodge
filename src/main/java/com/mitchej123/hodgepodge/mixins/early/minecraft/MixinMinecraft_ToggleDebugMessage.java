package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumChatFormatting;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizon.gtnhlib.GTNHLib;

@Mixin(Minecraft.class)
public class MixinMinecraft_ToggleDebugMessage {

    @Shadow
    public GameSettings options;

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;advancedItemTooltips:Z",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER))
    public void hodgepodge$printDebugChatMsgTooltips(CallbackInfo ci) {
        GTNHLib.proxy.addDebugToChat(
                "Advanced Item Tooltips:" + (options.advancedItemTooltips ? EnumChatFormatting.GREEN + " On"
                        : EnumChatFormatting.RED + " Off"));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderHitboxes:Z",
                    opcode = Opcodes.PUTSTATIC,
                    shift = At.Shift.AFTER))
    public void hodgepodge$printDebugChatMsgHitbox(CallbackInfo ci) {
        GTNHLib.proxy.addDebugToChat(
                "Hitboxes:" + (RenderManager.renderHitboxes ? EnumChatFormatting.GREEN + " On"
                        : EnumChatFormatting.RED + " Off"));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;pauseOnUnfocus:Z",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER))
    public void hodgepodge$printDebugChatMsgPauseLostFocus(CallbackInfo ci) {
        GTNHLib.proxy.addDebugToChat(
                "Pause on lost focus:" + (options.pauseOnUnfocus ? EnumChatFormatting.GREEN + " On"
                        : EnumChatFormatting.RED + " Off"));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;reload()V",
                    shift = At.Shift.AFTER))
    public void hodgepodge$printDebugChatMsgChunkReload(CallbackInfo ci) {
        GTNHLib.proxy.addDebugToChat("Reloading all chunks");
    }
}
