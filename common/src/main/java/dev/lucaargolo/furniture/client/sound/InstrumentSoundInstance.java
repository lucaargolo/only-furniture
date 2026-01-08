package dev.lucaargolo.furniture.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class InstrumentSoundInstance extends AbstractTickableSoundInstance {

    private final int release;
    private int age = 0;

    public InstrumentSoundInstance(SoundEvent event, float pitch, int release) {
        super(event, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.pitch = pitch;
        this.release = release;
    }

    @Override
    public void tick() {
        this.age++;
    }

    @Override
    public float getVolume() {
        if(this.age > this.release) {
            int i = this.age - this.release;
            return Mth.clamp(super.getVolume() - (i * 0.25f), 0f, 1f);
        }
        return super.getVolume();
    }

    @Override
    public boolean isStopped() {
        return this.age > this.release+4;
    }

}
