package dev.lucaargolo.furniture.sound;

import dev.lucaargolo.furniture.registry.ModRegistry;
import net.minecraft.sounds.SoundEvent;

public class ModInstruments {

    private final Instrument piano;

    public ModInstruments(ModRegistry<SoundEvent> registry) {
        this.piano = Instrument.register(registry, "piano");
    }

    public Instrument getPiano() {
        return this.piano;
    }

}
