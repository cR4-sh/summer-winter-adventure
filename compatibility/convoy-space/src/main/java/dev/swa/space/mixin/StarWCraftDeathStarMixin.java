package dev.swa.space.mixin;

import com.starwarsmod.world.deathstar.DeathStar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DeathStar.class, remap = false)
abstract class StarWCraftDeathStarMixin {
    @Inject(method = "board", at = @At("HEAD"), cancellable = true)
    private static void swa$disableLegacyTractorBeam(ServerPlayer player, CallbackInfo callback) {
        callback.cancel();
    }

    @Inject(method = "insideTick", at = @At(value = "INVOKE", target = "Lcom/starwarsmod/world/dimension/PlanetTeleporter;travelTo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/phys/Vec3;)V"), cancellable = true)
    private static void swa$disableLegacyHangarExit(ServerPlayer player, ServerLevel level, CallbackInfo callback) {
        callback.cancel();
    }
}
