package dev.lucaargolo.furniture.sound;

import com.google.common.collect.ImmutableMap;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.ModRegistry;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.*;

public record Instrument(Map<Key, MinecraftEntry<SoundEvent>> sounds) {

    public static final List<Instrument> INSTRUMENTS = new ArrayList<>();

    public Instrument(Map<Key, MinecraftEntry<SoundEvent>> sounds) {
        this.sounds = ImmutableMap.copyOf(sounds);
        INSTRUMENTS.add(this);
    }

    public record Key(Note note, DynamicLayer dynamicLayer, RoundRobin roundRobin) {

        public String path() {
            String n = note.name().toLowerCase(Locale.ROOT);
            String d = dynamicLayer.name().toLowerCase(Locale.ROOT);
            String r = roundRobin.name().toLowerCase(Locale.ROOT);
            return n + "_" + d + "_" + r;
        }

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

    public enum DynamicLayer {
        DYN1, DYN2, DYN3, DYN4
    }

    public enum RoundRobin {
        RR1, RR2
    }

    public void play(float volume, int semitone, TriConsumer<SoundEvent, Float, Float> consumer) {
        Key best = null;
        SoundEvent bestSound = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Map.Entry<Key, MinecraftEntry<SoundEvent>> entry : sounds.entrySet()) {
            if (entry.getKey().dynamicLayer() != DynamicLayer.DYN4) continue;
            if (entry.getKey().roundRobin() != RoundRobin.RR1) continue;

            int distance = Math.abs(entry.getKey().note().semitone() - semitone);

            if (distance < bestDistance) {
                best = entry.getKey();
                bestSound = entry.getValue().get();
                bestDistance = distance;
            }
        }

        if (bestSound != null) {
            int semitoneShift = semitone - best.note().semitone();
            float pitch = (float) Math.pow(2.0, semitoneShift / 12.0);

            consumer.accept(bestSound, volume, pitch);
        }
    }

    public static Instrument register(ModRegistry<SoundEvent> registry, String path) {
        Map<Key, MinecraftEntry<SoundEvent>> map = new HashMap<>();
        for (Note n : Note.values()) {
            for (DynamicLayer d : DynamicLayer.values()) {
                for (RoundRobin r : RoundRobin.values()) {
                    Key key = new Key(n, d, r);
                    ResourceLocation id = FurnitureMod.id(path + "_" + key.path());
                    map.put(key, registry.register(id.getPath(), () -> SoundEvent.createVariableRangeEvent(id)));
                }
            }
        }
        return new Instrument(map);
    }

    public interface TriConsumer<K, V, S> {
        void accept(K k, V v, S s);
    }

}
