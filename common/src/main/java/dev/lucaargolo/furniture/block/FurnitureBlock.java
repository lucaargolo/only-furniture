package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.data.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FurnitureBlock extends Block {

    public FurnitureBlock(Block base) {
        super(BlockBehaviour.Properties.ofFullCopy(base).noOcclusion());
    }

    @Override
    protected void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
        FurnitureData.set(pLevel, pPos, FurnitureData.DEFAULT);
    }

}
