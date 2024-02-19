package com.mitchej123.hodgepodge.mixins.late.witchery;

import static net.minecraft.client.entity.AbstractClientPlayer.loadTexture;

import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.emoniph.witchery.entity.EntityReflection;

@Mixin(EntityReflection.class)
public class MixinEntityReflection {

    @Redirect(
            method = "setupCustomSkin",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/emoniph/witchery/entity/EntityReflection;getDownloadImageSkin(Lnet/minecraft/util/ResourceLocation;Ljava/lang/String;)Lnet/minecraft/client/renderer/ThreadDownloadImageData;"),
            remap = false)
    private ThreadDownloadImageData hodgepodge$getDownloadImageSkin(ResourceLocation location, String name) {
        return loadTexture(location, name);
    }
}
