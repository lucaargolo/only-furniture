package dev.lucaargolo.furniture.block.behaviour;

import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Behaviour<I extends Behaviour<I>> {

    protected final Vec3 pos;

    public Behaviour(Vec3 pos) {
        this.pos = pos;
    }

    public Vec3 pos() {
        return pos;
    }

    public abstract I positioned(Vec3 pos);

    public abstract boolean interact(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index);

    public void remove(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index) {

    }

    public boolean isBlockEntityNeeded() {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Behaviour<?> that = (Behaviour<?>) object;
        return Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }

}
