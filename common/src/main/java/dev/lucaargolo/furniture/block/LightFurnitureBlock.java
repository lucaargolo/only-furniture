package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LightFurnitureBlock extends FurnitureBlock {

    public LightFurnitureBlock(Block base, VoxelShape[] shapes) {
        super(BlockBehaviour.Properties.ofFullCopy(base).lightLevel(s -> 15).dynamicShape().noTerrainParticles(), shapes);
    }

}
