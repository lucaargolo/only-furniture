package dev.lucaargolo.furniture.sound;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.ModRegistry;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ModSounds {

    public static final ModRegistry<SoundEvent> REGISTRY = FurnitureMod.registry(Registries.SOUND_EVENT);

    public static final Map<Key, MinecraftEntry<SoundEvent>> PIANO = registerForNote("piano");

    private static Map<Key, MinecraftEntry<SoundEvent>> registerForNote(String instrument) {
        Map<Key, MinecraftEntry<SoundEvent>> map = new HashMap<>();
        for (Note n : Note.values()) {
            for(DynamicLayer d : DynamicLayer.values()) {
                for(RoundRobin r : RoundRobin.values()) {
                    Key key = new Key(n, d, r);
                    ResourceLocation id = FurnitureMod.id(instrument + "_" + key.path());
                    map.put(key, REGISTRY.register(id.getPath(), () -> SoundEvent.createVariableRangeEvent(id)));
                }
            }
        }
        return map;
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
        A1, AS5, B2, C0, CS4, D1, DS5, E2, F6, FS3, G0, GS4
    }

    public enum DynamicLayer {
        DYN1, DYN2, DYN3, DYN4
    }

    public enum RoundRobin {
        RR1, RR2
    }

}
