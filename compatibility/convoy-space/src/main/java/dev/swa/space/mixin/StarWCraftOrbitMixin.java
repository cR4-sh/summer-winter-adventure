package dev.swa.space.mixin;

import com.starwarsmod.space.SpaceManager;
import com.starwarsmod.space.SpaceMap;
import dev.swa.space.LegacyStarWCraftTravel;
import java.util.List;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpaceManager.class, remap = false)
abstract class StarWCraftOrbitMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lcom/starwarsmod/space/SpaceMap;byDim(Lnet/minecraft/resources/ResourceKey;)Lcom/starwarsmod/space/SpaceMap$Planet;"), cancellable = true)
    private static void swa$skipLegacyDeparture(PlayerTickEvent.Post event, CallbackInfo callback) {
        var player = event.getEntity();
        if (player.tickCount % 60 == 0) {
            LegacyStarWCraftTravel.showNavigationHint(player);
        }
        callback.cancel();
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lcom/starwarsmod/space/SpaceMap;PLANETS:Ljava/util/List;"))
    private static List<SpaceMap.Planet> swa$skipLegacyApproach() {
        return List.of();
    }
}
