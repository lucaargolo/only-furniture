package dev.lucaargolo.furniture.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PlantHolderBlockEntity extends BlockEntity {

    public PlantHolderBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PLANT_HOLDER.get(), pos, blockState);
    }

}
