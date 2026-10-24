package de.enderpickup;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TogglePayload() implements CustomPayload {
    public static final CustomPayload.Id<TogglePayload> ID =
            new CustomPayload.Id<>(Identifier.of("enderpickup", "toggle"));
    public static final PacketCodec<RegistryByteBuf, TogglePayload> CODEC =
            PacketCodec.unit(new TogglePayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
