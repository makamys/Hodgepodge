package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mitchej123.hodgepodge.config.TweaksConfig;

@Mixin(Minecraft.class)
public class MixinMinecraft_FastBlockPlacing {

    @Shadow
    private int itemUseDelay;
    @Shadow
    public MovingObjectPosition crosshairTarget;
    @Shadow
    public EntityClientPlayerMP player;
    @Shadow
    public GameSettings options;

    private Vec3 lastPosition;
    private ForgeDirection lastSide;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcpw/mods/fml/common/FMLCommonHandler;onPreClientTick()V",
                    shift = At.Shift.AFTER))
    private void hodgepodge$func_147121_ag(CallbackInfo ci) {
        if (!TweaksConfig.fastBlockPlacing) return;
        if (player == null || player.isUsingItem()) return;
        if (crosshairTarget == null) return;
        if (crosshairTarget.type != MovingObjectPosition.MovingObjectType.BLOCK) return;

        ItemStack itemstack = this.player.inventory.getMainHandStack();

        if (itemstack == null) return;
        if (!(itemstack.getItem() instanceof ItemBlock)) return;

        Vec3 pos = createVec3(crosshairTarget);
        if (itemUseDelay > 0 && !isPosEqual(pos, lastPosition)
                && (lastPosition == null || !isPosEqual(pos, getNewPosition(lastPosition, lastSide)))) {
            itemUseDelay = 0;
        } else if (itemUseDelay == 0 && isPosEqual(pos, lastPosition)
                && lastSide.equals(ForgeDirection.getOrientation(crosshairTarget.face))) {
                    itemUseDelay = 4;
                }

        lastPosition = pos;
        lastSide = ForgeDirection.getOrientation(crosshairTarget.face);
    }

    private Vec3 createVec3(MovingObjectPosition pos) {
        return Vec3.of(pos.x, pos.y, pos.z);
    }

    private boolean isPosEqual(Vec3 p1, Vec3 p2) {
        if (p1 == null || p2 == null) {
            return false;
        }
        return p1.x == p2.x && p1.y == p2.y && p1.z == p2.z;
    }

    private Vec3 getNewPosition(Vec3 pos, ForgeDirection direction) {
        return Vec3.of(
                pos.x + direction.offsetX,
                pos.y + direction.offsetY,
                pos.z + direction.offsetZ);
    }
}
