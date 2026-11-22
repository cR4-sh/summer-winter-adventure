package dev.swa.forcetraining;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = ForceTraining.ID)
public record TrainingPreviewPayload(int selection) implements CustomPacketPayload {
    public static final Type<TrainingPreviewPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ForceTraining.ID, "preview"));
    private static final StreamCodec<FriendlyByteBuf, TrainingPreviewPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, TrainingPreviewPayload::selection, TrainingPreviewPayload::new);

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToClient(TYPE, CODEC, (payload, context) -> ForceTrainingClient.preview(payload.selection));
    }

    @Override
    public Type<TrainingPreviewPayload> type() {
        return TYPE;
    }
}
