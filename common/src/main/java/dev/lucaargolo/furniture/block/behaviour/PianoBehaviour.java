package dev.lucaargolo.furniture.block.behaviour;

import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.sound.Instrument;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PianoBehaviour extends Behaviour<PianoBehaviour> {

    private final Instrument instrument;
    private final int semitone;

    public PianoBehaviour(Vec3 pos, Instrument instrument, int semitone) {
        super(pos);
        this.instrument = instrument;
        this.semitone = semitone;
    }

    @Override
    public PianoBehaviour positioned(Vec3 pos) {
        return new PianoBehaviour(pos, this.instrument, this.semitone);
    }

    @Override
    public boolean interact(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index) {
        this.instrument.play(1f, this.semitone, (sound, volume, pitch) -> {
            level.playSound(player, pos, sound, SoundSource.BLOCKS, volume, pitch);
        });
        return true;
    }

}
