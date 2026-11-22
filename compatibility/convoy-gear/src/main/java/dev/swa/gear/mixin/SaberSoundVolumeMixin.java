package dev.swa.gear.mixin;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSoundInstance.class)
abstract class SaberSoundVolumeMixin {
    @Shadow public abstract ResourceLocation getLocation();

    @Inject(method = "getVolume", at = @At("RETURN"), cancellable = true)
    private void preview$attenuateSaber(CallbackInfoReturnable<Float> result) {
        ResourceLocation sound = getLocation();
        if (!sound.getNamespace().equals("starwarsmod")) return;
        float gain = switch (sound.getPath()) {
            case "blaster_shoot" -> 0.45F;
            case "lightsaber_hum" -> 0.10F;
            case "lightsaber_open" -> 0.32F;
            case "lightsaber_close" -> 0.28F;
            default -> 1.0F;
        };
        if (gain != 1F) result.setReturnValue(result.getReturnValueF() * gain);
    }
}
