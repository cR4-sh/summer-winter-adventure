package dev.swa.forcetraining.mixin;

import dev.swa.forcetraining.ForceTraining;

import com.starwarsmod.force.ForceData;
import com.starwarsmod.force.ForcePowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForcePowers.class, remap = false)
abstract class TrainingPowerRules {
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void training$ignoreTrainingProgression(ForceData data, CallbackInfoReturnable<Boolean> result) {
        var power = (ForcePowers) (Object) this;
        if (ForceTraining.bypassProgression(data, power)) {
            result.setReturnValue(data.getEnergy() >= data.powerCost(power));
        }
    }
}
