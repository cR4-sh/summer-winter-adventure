package dev.swa.space.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.starwarsmod.force.ForceData;
import com.starwarsmod.force.ForcePowers;
import dev.swa.space.ConvoyForceAnimations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ForcePowers.class, remap = false)
abstract class ConvoyForceCastMixin {
    @WrapMethod(method = "execute")
    private boolean swa$playAcceptedCast(Player player, ForceData data, Operation<Boolean> original) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return original.call(player, data);
        }
        var animation = ConvoyForceAnimations.cast((ForcePowers) (Object) this);
        var patch = ConvoyForceAnimations.battlePatch(serverPlayer);
        if (animation == null || patch == null) {
            return original.call(player, data);
        }

        var previousCaster = ConvoyForceAnimations.setCaster(serverPlayer);
        boolean accepted;
        try {
            accepted = original.call(player, data);
        } finally {
            ConvoyForceAnimations.setCaster(previousCaster);
        }
        if (accepted) {
            patch.playAnimationSynchronized(animation, 0.0F);
        }
        return accepted;
    }
}
