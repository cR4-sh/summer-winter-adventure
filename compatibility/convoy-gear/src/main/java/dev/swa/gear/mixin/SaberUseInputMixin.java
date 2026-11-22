package dev.swa.gear.mixin;

import com.starwarsmod.item.LightsaberItem;
import dev.swa.gear.SaberClientControls;
import dev.swa.gear.SaberTogglePayload;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
abstract class SaberUseInputMixin {
    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void preview$separateSaberUse(Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> result) {
        var stack = player.getItemInHand(hand);
        if (SaberClientControls.hasToggleChannel() && stack.getItem() instanceof LightsaberItem
                && !SaberTogglePayload.usesHybridFire(player, stack)) {
            // Guard owns this click. Do not send a vanilla packet that also toggles the blade.
            result.setReturnValue(InteractionResult.CONSUME);
        }
    }
}
