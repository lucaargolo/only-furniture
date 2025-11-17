package dev.lucaargolo.furniture.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FurnitureBlockEntity extends BlockEntity {

    public FurnitureBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FURNITURE.get(), pos, blockState);
    }

}
