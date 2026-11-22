package dev.swa.gear.mixin;

import dev.swa.gear.StarWarsArmor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientHooks.class)
abstract class ArmorTextureMixin {
    @Inject(method = "getArmorTexture", at = @At("HEAD"), cancellable = true)
    private static void preview$armorTexture(Entity entity, ItemStack stack, ArmorMaterial.Layer layer,
            boolean inner, EquipmentSlot slot, CallbackInfoReturnable<ResourceLocation> callback) {
        ResourceLocation texture = StarWarsArmor.texture(stack);
        if (texture != null) callback.setReturnValue(texture);
    }
}
