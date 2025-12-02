package dev.lucaargolo.furniture.network;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.sound.Instrument;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record PlayInstrumentPayload(Instrument instrument, int semitone, int release) implements CustomPacketPayload {

    public static final Type<PlayInstrumentPayload> TYPE = new Type<>(FurnitureMod.id("play_instrument_payload"));

    public static final StreamCodec<ByteBuf, PlayInstrumentPayload> STREAM_CODEC = StreamCodec.composite(
            Instrument.STREAM_CODEC,
            PlayInstrumentPayload::instrument,
            ByteBufCodecs.VAR_INT,
            PlayInstrumentPayload::semitone,
            ByteBufCodecs.VAR_INT,
            PlayInstrumentPayload::release,
            PlayInstrumentPayload::new
    );

    public static void handleClient(PlayInstrumentPayload payload, Executor executor) {
        executor.execute(() -> {
            payload.instrument.play(payload.semitone, payload.release);
        });
    }

    @Override
    public @NotNull Type<PlayInstrumentPayload> type() {
        return TYPE;
    }

}
