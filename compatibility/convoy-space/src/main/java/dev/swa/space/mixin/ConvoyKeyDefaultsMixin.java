package dev.swa.space.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.swa.space.ConvoySpace;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
abstract class ConvoyKeyDefaultsMixin {
    @Shadow public KeyMapping[] keyMappings;

    @Unique private static final Map<String, InputConstants.Key> swa$keyDefaults = swa$loadKeyDefaults();

    @Inject(method = "load(Z)V", at = @At("HEAD"))
    private void swa$useConvoyDefaults(boolean includeKeys, CallbackInfo callback) {
        if (!includeKeys) return;
        for (KeyMapping mapping : keyMappings) {
            InputConstants.Key replacement = swa$keyDefaults.get(mapping.getName());
            if (replacement == null || mapping.getDefaultKey().equals(replacement)) continue;
            if (mapping.isDefault()) mapping.setKey(replacement);
            ((ConvoyKeyMappingAccessor) mapping).swa$setDefaultKey(replacement);
        }
        KeyMapping.resetMapping();
    }

    @Unique
    private static Map<String, InputConstants.Key> swa$loadKeyDefaults() {
        var resource = Objects.requireNonNull(ConvoySpace.class.getResourceAsStream("/swa_convoy_key_defaults.tsv"));
        var input = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
        try (input) {
            var defaults = new HashMap<String, InputConstants.Key>();
            for (String line : input.lines().toList()) {
                String[] fields = line.split("\t", 2);
                defaults.put(fields[0], InputConstants.getKey(fields[1]));
            }
            return Map.copyOf(defaults);
        } catch (java.io.IOException error) {
            throw new java.io.UncheckedIOException(error);
        }
    }
}
