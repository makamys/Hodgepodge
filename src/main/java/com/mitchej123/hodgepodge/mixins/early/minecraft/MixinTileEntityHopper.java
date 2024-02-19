package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityHopper.class)
public class MixinTileEntityHopper {

    /**
     * @author MuXiu1997
     * @reason Full hopper voiding items from drawer
     */
    @Redirect(
            method = "pullItems(Lnet/minecraft/tileentity/IHopper;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/TileEntityHopper;pullItems(Lnet/minecraft/tileentity/IHopper;Lnet/minecraft/inventory/IInventory;II)Z"))
    private static boolean hodgepodge$moveFromHopperToInventory(IHopper hopper, IInventory inventory, int slot,
            int side) {
        ItemStack is = inventory.getStack(slot);
        if (is == null || is.size == 0) return false;
        if (inventory instanceof ISidedInventory) {
            ISidedInventory sidedInventory = (ISidedInventory) inventory;
            if (!sidedInventory.canHopperRemoveStack(slot, is, side)) return false;
        }
        int spaceSlot = getSpaceSlot(hopper, is);
        if (spaceSlot == -1) return false;
        ItemStack decreased = inventory.removeStack(slot, 1);
        if (decreased == null || decreased.size == 0) return false;
        ItemStack space = hopper.getStack(spaceSlot);
        if (space == null) {
            space = is.copy();
            space.size = 1;
            hopper.setStack(spaceSlot, space);
        } else {
            space.size += 1;
        }
        return true;
    }

    @Unique
    private static int getSpaceSlot(IHopper hopper, ItemStack is) {
        final int size = hopper.getSize();
        for (int i = 0; i < size; i++) {
            ItemStack space = hopper.getStack(i);
            if (space == null) {
                return i;
            }
            if (space.size < Math.min(space.getMaxSize(), hopper.getMaxStackSize()))
                if (is.matchesItem(space) && ItemStack.matchesNbt(is, space)) {
                    return i;
                }
        }
        return -1;
    }
}
