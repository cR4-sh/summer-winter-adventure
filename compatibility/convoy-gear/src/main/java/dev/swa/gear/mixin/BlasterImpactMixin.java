package dev.swa.gear.mixin;

import com.starwarsmod.entity.BlasterBolt;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlasterBolt.class)
abstract class BlasterImpactMixin {
    @Inject(method = "onHitBlock", at = @At("HEAD"))
    private void preview$removeArrowImpact(BlockHitResult hit, CallbackInfo callback) {
        ((BlasterBolt) (Object) this).setSoundEvent(SoundEvents.EMPTY);
    }
}
