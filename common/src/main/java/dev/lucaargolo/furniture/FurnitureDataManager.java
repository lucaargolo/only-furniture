package dev.lucaargolo.furniture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public abstract class FurnitureDataManager {

    public void init() {

    }

    public abstract FurnitureData[] get(LevelReader level, BlockPos pos);

    public abstract void set(LevelReader level, BlockPos pos, FurnitureData[] layers);

    public abstract VoxelShape cachedShape(LevelReader level, BlockPos pos, Supplier<VoxelShape> shapeSupplier);

    protected abstract ChunkFurnitureData getChunkData(LevelReader level, ChunkPos chunkPos);

}
