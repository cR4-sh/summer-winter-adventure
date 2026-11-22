package dev.swa.gear;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@EventBusSubscriber(modid = GearPreview.ID)
public record BlasterShotPayload(int boltId, Vec3 origin, Vec3 direction, Vec3 fallbackMuzzle, float red, float green,
        float blue, boolean critical) implements CustomPacketPayload {
    public static final Type<BlasterShotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GearPreview.ID, "blaster_shot"));
    private static final StreamCodec<RegistryFriendlyByteBuf, BlasterShotPayload> CODEC = StreamCodec.ofMember(
            BlasterShotPayload::encode, BlasterShotPayload::decode);

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(TYPE, CODEC, (payload, context) -> BlasterMuzzle.receive(payload));
    }

    public static boolean supported(ServerPlayer player) {
        return player.connection != null && NetworkRegistry.hasChannel(player.connection, TYPE.id());
    }

    @Override
    public Type<BlasterShotPayload> type() {
        return TYPE;
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(boltId);
        writeVector(buffer, origin);
        writeVector(buffer, direction);
        writeVector(buffer, fallbackMuzzle);
        buffer.writeFloat(red);
        buffer.writeFloat(green);
        buffer.writeFloat(blue);
        buffer.writeBoolean(critical);
    }

    private static BlasterShotPayload decode(RegistryFriendlyByteBuf buffer) {
        return new BlasterShotPayload(buffer.readVarInt(), readVector(buffer), readVector(buffer), readVector(buffer),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    private static void writeVector(RegistryFriendlyByteBuf buffer, Vec3 value) {
        buffer.writeDouble(value.x);
        buffer.writeDouble(value.y);
        buffer.writeDouble(value.z);
    }

    private static Vec3 readVector(RegistryFriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
}
