package dev.lucaargolo.furniture.sound;

import com.google.common.collect.ImmutableMap;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.ModRegistry;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record Instrument(int index, Map<Note, MinecraftEntry<SoundEvent>> sounds) {

    public static final List<Instrument> INSTRUMENTS = new LinkedList<>();

    public static final StreamCodec<ByteBuf, Instrument> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull Instrument decode(@NotNull ByteBuf buffer) {
            return INSTRUMENTS.get(buffer.readInt());
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, @NotNull Instrument value) {
            buffer.writeInt(value.index());
        }
    };

    public Instrument(Map<Note, MinecraftEntry<SoundEvent>> sounds) {
        this(INSTRUMENTS.size(), ImmutableMap.copyOf(sounds));
        INSTRUMENTS.add(this);
    }

    public enum Note {
        C0(12), G0(19),
        D1(26), A1(33),
        E2(40), B2(47),
        FS3(54),
        CS4(61), GS4(68),
        DS5(75), AS5(82),
        F6(89);

        private final int semitone;

        Note(int semitone) {
            this.semitone = semitone;
        }

        public int semitone() {
            return semitone;
        }
    }

    public void play(int semitone, int release) {
        Note best = null;
        SoundEvent bestSound = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Map.Entry<Note, MinecraftEntry<SoundEvent>> entry : sounds.entrySet()) {
            int distance = Math.abs(entry.getKey().semitone() - semitone);

            if (distance < bestDistance) {
                best = entry.getKey();
                bestSound = entry.getValue().get();
                bestDistance = distance;
            }
        }

        if (bestSound != null) {
            int semitoneShift = semitone - best.semitone();
            float pitch = (float) Math.pow(2.0, semitoneShift / 12.0);

            Minecraft.getInstance().getSoundManager().queueTickingSound(new InstrumentSoundInstance(bestSound, pitch, release));
        }
    }

    public static Instrument register(ModRegistry<SoundEvent> registry, String path) {
        Map<Note, MinecraftEntry<SoundEvent>> map = new HashMap<>();
        for (Note key : Note.values()) {
            ResourceLocation id = FurnitureMod.id(path + "_" + key.name().toLowerCase(Locale.ROOT));
            map.put(key, registry.register(id.getPath(), () -> SoundEvent.createVariableRangeEvent(id)));
        }
        return new Instrument(map);
    }

}
