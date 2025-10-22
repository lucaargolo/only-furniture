package dev.lucaargolo.furniture.item;

import dev.lucaargolo.furniture.data.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FurnitureBlockItem extends BlockItem {

    public FurnitureBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected boolean placeBlock(@NotNull BlockPlaceContext pContext, @NotNull BlockState pState) {
        boolean placed = super.placeBlock(pContext, pState);
        BlockPos pos = pContext.getClickedPos();
        Vec3 location = pContext.getClickLocation();
        FurnitureData.set(pContext.getLevel(), pos, new FurnitureData((float) (location.x - pos.getX()), (float) (location.z - pos.getZ()), 0));
        return placed;
    }

}
