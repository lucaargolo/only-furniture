package dev.lucaargolo.furniture.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class InstrumentSoundInstance extends AbstractTickableSoundInstance {

    private final int duration;
    private int age = 0;

    protected InstrumentSoundInstance(SoundEvent event, float pitch, int duration) {
        super(event, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.pitch = pitch;
        this.duration = duration;
    }

    @Override
    public void tick() {
        this.age++;
    }

    @Override
    public boolean isStopped() {
        return this.age > this.duration;
    }

}
