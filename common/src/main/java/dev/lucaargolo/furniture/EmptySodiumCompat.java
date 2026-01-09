package dev.lucaargolo.furniture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public class EmptySodiumCompat extends SodiumCompat {

    @Override
    @Nullable
    public FurnitureData[] get(BlockGetter getter, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isPresent() {
        return false;
    }
}
