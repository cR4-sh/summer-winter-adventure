package dev.swa.gear.mixin;

import com.starwarsmod.item.LightsaberItem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = GuardSkill.class, remap = false)
abstract class SaberSustainedGuardMixin {
    @Inject(method = "isExecutableState", at = @At("HEAD"), cancellable = true)
    private void preview$requireLitBladeOrShield(PlayerPatch<?> patch, CallbackInfoReturnable<Boolean> result) {
        Player player = patch.getOriginal();
        var main = player.getMainHandItem();
        if (main.getItem() instanceof LightsaberItem && !LightsaberItem.isActive(main)) {
            result.setReturnValue(false);
        }
    }
}
