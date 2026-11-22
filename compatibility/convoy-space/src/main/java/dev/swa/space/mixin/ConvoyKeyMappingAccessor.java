package dev.swa.space.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
interface ConvoyKeyMappingAccessor {
    @Mutable
    @Accessor("defaultKey")
    void swa$setDefaultKey(InputConstants.Key key);
}
