package dev.swa.gear.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.starwarsmod.item.LightsaberItem")
abstract class SaberAmbientParticleMixin {
    @Redirect(method = "inventoryTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void preview$keepIdleSparksOutOfTheFirstPersonView(Level level, ParticleOptions particle,
            double x, double y, double z, double dx, double dy, double dz,
            ItemStack item, Level ignored, Entity holder, int slot, boolean selected) {
        var client = Minecraft.getInstance();
        if (holder != client.player || !client.options.getCameraType().isFirstPerson()) {
            level.addParticle(particle, x, y, z, dx, dy, dz);
        }
    }
}
