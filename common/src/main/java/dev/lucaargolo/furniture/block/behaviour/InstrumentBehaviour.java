package dev.lucaargolo.furniture.block.behaviour;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.network.PlayInstrumentPayload;
import dev.lucaargolo.furniture.sound.Instrument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class InstrumentBehaviour extends Behaviour<InstrumentBehaviour> {

    private final Instrument instrument;
    private final int semitone;

    public InstrumentBehaviour(Vec3 pos, Instrument instrument, int semitone) {
        super(pos);
        this.instrument = instrument;
        this.semitone = semitone;
    }

    @Override
    public InstrumentBehaviour positioned(Vec3 pos) {
        return new InstrumentBehaviour(pos, this.instrument, this.semitone);
    }

    @Override
    public boolean interact(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index) {
        if(level.isClientSide()) {
            this.instrument.play(this.semitone, 5);
        }else{
            FurnitureMod.getPacketManager().sendToPlayersTrackingEntity(player, new PlayInstrumentPayload(this.instrument, this.semitone, 5));
        }
        return true;
    }

}
