package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LightFurnitureBlock extends FurnitureBlock {

    public LightFurnitureBlock(Block.Properties properties, VoxelShape[] shapes) {
        super(properties, shapes);
    }

    public LightFurnitureBlock(Block base, VoxelShape[] shapes) {
        super(BlockBehaviour.Properties.ofFullCopy(base).lightLevel(s -> 15), shapes);
    }

}
