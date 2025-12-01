package dev.lucaargolo.furniture.block.behaviour;

import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PianoBehaviour extends Behaviour<PianoBehaviour> {

    public PianoBehaviour(Vec3 pos) {
        super(pos);
    }

    @Override
    public PianoBehaviour positioned(Vec3 pos) {
        return null;
    }

    @Override
    public boolean interact(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index) {
        return false;
    }

}
