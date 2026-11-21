package dev.swa.space.mixin;

import dev.swa.space.ConvoyCombat;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = GuardSkill.class, remap = false)
abstract class ConvoyEpicFightGuardMixin {
    @Inject(method = "isExecutableState", at = @At("HEAD"), cancellable = true)
    private void swa$leaveMountedControlsAlone(PlayerPatch<?> patch, CallbackInfoReturnable<Boolean> callback) {
        if (patch.getOriginal().isPassenger()) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "startHolding", at = @At("HEAD"))
    private void swa$lightSaberBeforeGuard(SkillContainer container, CallbackInfo callback) {
        if (container.getExecutor().getOriginal() instanceof ServerPlayer player) {
            ConvoyCombat.igniteSaber(player);
        }
    }
}
