package dev.lucaargolo.furniture.block.base;

import net.minecraft.world.level.block.state.BlockState;

public interface LightBlock {

    default int getLight(BlockState state) {
        return 15;
    }

}
