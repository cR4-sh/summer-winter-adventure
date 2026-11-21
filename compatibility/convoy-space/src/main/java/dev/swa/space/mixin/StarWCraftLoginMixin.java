package dev.swa.space.mixin;

import com.starwarsmod.StarWarsModEvents;
import com.starwarsmod.force.ForceData;
import dev.swa.space.ConvoyOnboarding;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StarWarsModEvents.class, remap = false)
abstract class StarWCraftLoginMixin {
    @Redirect(method = "onLogin", at = @At(value = "INVOKE",
        target = "Lcom/starwarsmod/force/ForceData;reloadFromPlayer(Lnet/minecraft/world/entity/player/Player;)Lcom/starwarsmod/force/ForceData;"))
    private static ForceData swa$loadCampaignDefaults(Player player) {
        // Run before StarWCraft issues its introductory items and ForceData packet.
        return ConvoyOnboarding.load(player);
    }
}
