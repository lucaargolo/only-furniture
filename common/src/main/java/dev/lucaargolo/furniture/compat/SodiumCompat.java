package dev.lucaargolo.furniture.compat;

import dev.lucaargolo.furniture.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public abstract class SodiumCompat {

    @Nullable
    public abstract FurnitureData[] get(BlockGetter getter, BlockPos pos);

    public abstract boolean isPresent();

}
