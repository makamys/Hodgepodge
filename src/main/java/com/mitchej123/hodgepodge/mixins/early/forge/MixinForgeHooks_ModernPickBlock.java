package com.mitchej123.hodgepodge.mixins.early.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks_ModernPickBlock {

    // credit for original mixin: bearsdotzone
    @Inject(method = "onPickBlock", at = @At(value = "RETURN", ordinal = 4), cancellable = true)
    private static void hodgepodge$onPickBlock(MovingObjectPosition target, EntityPlayer player, World world,
            CallbackInfoReturnable<Boolean> cir, @Local(name = "result") LocalRef<ItemStack> result) {
        Minecraft clientObject = Minecraft.getInstance();
        for (int x = 9; x < 36; x++) {
            ItemStack stack = player.inventory.getStack(x);
            if (stack != null && stack.matchesItem(result.get())
                    && ItemStack.matchesNbt(stack, result.get())) {
                int moveSlot = player.inventory.selectedSlot;
                moveSlot = 36 + moveSlot;

                clientObject.interactionManager.clickSlot(player.playerMenu.networkId, x, 0, 0, player);
                clientObject.interactionManager.clickSlot(player.playerMenu.networkId, moveSlot, 0, 0, player);
                clientObject.interactionManager.clickSlot(player.playerMenu.networkId, x, 0, 0, player);
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
