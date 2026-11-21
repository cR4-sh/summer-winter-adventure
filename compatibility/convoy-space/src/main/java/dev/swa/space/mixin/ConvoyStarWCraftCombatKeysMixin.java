package dev.swa.space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "com.starwarsmod.client.StarWarsKeybinds", remap = false)
abstract class ConvoyStarWCraftCombatKeysMixin {
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 82))
    private static int swa$cycleForceOnBracket(int r) {
        return 91; // GLFW_KEY_LEFT_BRACKET. R remains Epic Fight's mode switch.
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 71))
    private static int swa$unbindLegacyGuard(int g) {
        return -1;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 342))
    private static int swa$unbindLegacyDodge(int leftAlt) {
        return -1;
    }
}
