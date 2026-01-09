package dev.lucaargolo.furniture.compat;

import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.mixin.sodium.LevelSliceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class FabricSodiumCompat extends SodiumCompat {

    @Override
    @Nullable
    public FurnitureData[] get(BlockGetter getter, BlockPos pos) {
        if(getter instanceof LevelSliceAccessor slice) {
            return FurnitureData.getChunkData(slice.getLevel(), new ChunkPos(pos)).get(pos);
        }else{
            return null;
        }
    }

    @Override
    public boolean isPresent() {
        return true;
    }

}
