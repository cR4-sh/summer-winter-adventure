package dev.swa.space.mixin;

import com.starwarsmod.combat.Anim;
import com.starwarsmod.combat.Combat;
import dev.swa.space.ConvoyForceAnimations;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Combat.class, remap = false)
abstract class ConvoyLegacyForcePoseMixin {
    @Inject(method = "anim", at = @At("HEAD"), cancellable = true)
    private static void swa$replaceCastPose(LivingEntity fighter, Anim animation, CallbackInfo callback) {
        if (ConvoyForceAnimations.replacesLegacyPose(fighter, animation)) {
            callback.cancel();
        }
    }
}
