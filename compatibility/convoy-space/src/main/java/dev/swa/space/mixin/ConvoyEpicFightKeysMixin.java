package dev.swa.space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "yesman.epicfight.client.input.EpicFightKeyMappings", remap = false)
abstract class ConvoyEpicFightKeysMixin {
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 342))
    private static int swa$rollOnC(int leftAlt) {
        return 67; // GLFW_KEY_C. Left Alt remains the Unbound mining beam.
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 71))
    private static int swa$lockOnF6(int g) {
        return 295; // GLFW_KEY_F6. G remains the Unbound equipment screen.
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 89))
    private static int swa$unbindEmoteWheel(int y) {
        return -1; // GLFW_KEY_UNKNOWN. Y remains StarWCraft guidance.
    }
}
