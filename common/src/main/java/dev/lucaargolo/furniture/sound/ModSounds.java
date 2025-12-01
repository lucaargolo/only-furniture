package dev.lucaargolo.furniture.sound;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.ModRegistry;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class ModSounds {

    public static final ModRegistry<SoundEvent> REGISTRY = FurnitureMod.registry(Registries.SOUND_EVENT);
    public static final MinecraftEntry<SoundEvent> EMPTY = new MinecraftEntry<>(-1, "empty", () -> SoundEvents.EMPTY);

    public static final Instrument PIANO = Instrument.register(REGISTRY, "piano");


}
