package dev.swa.space.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.starwarsmod.command.PlanetCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlanetCommand.class, remap = false)
abstract class StarWCraftPlanetCommandMixin {
    @Inject(method = {"lambda$register$4", "lambda$register$50"}, at = @At(value = "INVOKE", target = "Lcom/starwarsmod/world/dimension/PlanetTeleporter;travelTo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;)V"), cancellable = true)
    private static void swa$requireFlight(CommandContext<CommandSourceStack> context, CallbackInfoReturnable<Integer> callback) {
        context.getSource().sendFailure(Component.translatable("swa.space.use_unbound"));
        callback.setReturnValue(0);
    }
}
