package dev.swa.space.mixin;

import com.starwarsmod.world.dimension.PlanetTeleporter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlanetTeleporter.class, remap = false)
abstract class PlanetTeleporterMixin {
    @Inject(method = "travelTo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;)V", at = @At("HEAD"), cancellable = true)
    private static void swa$skipLegacyHyperspace(ServerPlayer player, ResourceKey<Level> destination, CallbackInfo callback) {
        if (!player.serverLevel().dimension().equals(destination)) {
            callback.cancel();
        }
    }
}
