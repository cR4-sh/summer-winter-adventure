package dev.swa.gear;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(GearPreview.ID)
public final class GearPreview {
    public static final String ID = "swa_gear_preview";
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ID);
    public static final net.neoforged.neoforge.registries.DeferredHolder<SoundEvent, SoundEvent> BLASTER_SHOT =
            SOUNDS.register("blaster_shoot", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ID, "blaster_shoot")));

    public GearPreview(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
