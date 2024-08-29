package com.nettakrim.souper_secret_settings.network.packets;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

/**
 * <p>This package provides functionality for applying shaders to the game from the server.</p>
 * <p>The package leverages a server-side plugin (e.g., DedCraftDrugsPlugin) to communicate shader
 * information to clients.</p>
 *
 * <h2>Example Usage:</h2>
 *
 * <pre>
 * {@code
 *  private void setShaderToClient(Player player, String shaderId) {
 *      byte[] buf = shaderId.getBytes(StandardCharsets.UTF_8);
 *      player.sendPluginMessage(HypeTrainTrapPlugin.PLUGIN, "souper_secret_settings:apply_shader_packet_id", buf);
 *  }
 * }
 * </pre>
 *
 * <p>The code above demonstrates sending a shader ID to a client using a plugin message.</p>
 *
 * @author RazorPlay01
 */
public record ApplyShaderPacket(String shaderId) implements CustomPayload {
    public static final Identifier APPLY_SHADER_PACKET_ID =
            Identifier.of(SouperSecretSettingsClient.MODID, "apply_shader_packet_id");

    public static final CustomPayload.Id<ApplyShaderPacket> PACKET_ID =
            new CustomPayload.Id<>(ApplyShaderPacket.APPLY_SHADER_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, ApplyShaderPacket> CODEC = PacketCodec.tuple(
            new PacketCodec<ByteBuf, String>() {

                public String decode(ByteBuf byteBuf) {
                    int readableBytes = byteBuf.readableBytes();
                    byte[] bytes = new byte[readableBytes];
                    byteBuf.readBytes(bytes);
                    return new String(bytes, StandardCharsets.UTF_8);
                }

                public void encode(ByteBuf byteBuf, String string) {
                    byteBuf.writeBytes(string.getBytes(StandardCharsets.UTF_8));
                }
            },
            ApplyShaderPacket::shaderId, ApplyShaderPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}