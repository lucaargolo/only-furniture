package dev.lucaargolo.furniture.block.interaction;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public abstract class Interaction<I extends Interaction<I>> {

    protected final Vec3 pos;

    public Interaction(Vec3 pos) {
        this.pos = pos;
    }

    public Vec3 pos() {
        return pos;
    }

    public abstract I positioned(Vec3 pos);

    public abstract boolean interact(int index, Level level, Player player, BlockHitResult hitResult);

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Interaction<?> that = (Interaction<?>) object;
        return Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }

}
