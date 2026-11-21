package dev.swa.space.mixin;

import com.starwarsmod.item.PlanetTravelItem;
import dev.swa.space.LegacyStarWCraftTravel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlanetTravelItem.class, remap = false)
abstract class HyperspaceBeaconMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void swa$useUnbound(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
        if (!level.isClientSide) {
            LegacyStarWCraftTravel.showNavigationHint(player);
        }
        callback.setReturnValue(InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide));
    }
}
