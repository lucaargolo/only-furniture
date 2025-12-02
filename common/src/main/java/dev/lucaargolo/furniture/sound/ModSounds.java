package dev.lucaargolo.furniture.sound;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final ModRegistry<SoundEvent> REGISTRY = FurnitureMod.registry(Registries.SOUND_EVENT);
    public static final ModInstruments INSTRUMENTS = new ModInstruments(REGISTRY);

}
