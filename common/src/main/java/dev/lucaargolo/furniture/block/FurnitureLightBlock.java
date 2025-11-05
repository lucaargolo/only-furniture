package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FurnitureLightBlock extends FurnitureBlock {

    public FurnitureLightBlock(Block.Properties properties, VoxelShape[] shapes) {
        super(properties, shapes);
    }

    public FurnitureLightBlock(Block base, VoxelShape[] shapes) {
        super(BlockBehaviour.Properties.ofFullCopy(base).lightLevel(s -> 15), shapes);
    }

}
