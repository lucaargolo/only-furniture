package dev.lucaargolo.furniture.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FurnitureBlockItem extends BlockItem {

    public FurnitureBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected boolean placeBlock(@NotNull BlockPlaceContext pContext, @NotNull BlockState pState) {
        boolean placed = super.placeBlock(pContext, pState);
        return placed;
    }
}
