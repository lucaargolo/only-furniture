package dev.lucaargolo.furniture.block.interaction;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface Interaction<I extends Interaction<I>> {

    Vec3 pos();

    I positioned(Vec3 pos);

    boolean interact(Level level, Player player, BlockHitResult hitResult);

}
