package dev.swa.space.mixin;

import dev.swa.space.ConvoyCombat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.common.ComboAttacks;

@Mixin(value = ComboAttacks.class, remap = false)
abstract class ConvoyEpicFightComboMixin {
    @Inject(method = "executeOnServer", at = @At("HEAD"))
    private void swa$lightSaberBeforeSwing(SkillContainer container, CompoundTag arguments, CallbackInfo callback) {
        ServerPlayer player = (ServerPlayer) container.getServerExecutor().getOriginal();
        if (!player.isPassenger()) {
            ConvoyCombat.igniteSaber(player);
        }
    }
}
