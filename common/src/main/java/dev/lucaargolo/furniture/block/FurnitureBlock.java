package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.data.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class FurnitureBlock extends Block {

    private final VoxelShape shape;

    public FurnitureBlock(Block base, VoxelShape... shapes) {
        super(BlockBehaviour.Properties.ofFullCopy(base).noOcclusion());
        VoxelShape shape = Shapes.empty();
        for (VoxelShape s : shapes) {
            shape = Shapes.join(shape, s, BooleanOp.OR);
        }
        this.shape = shape;
    }

    @Override
    protected void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
        FurnitureData.set(pLevel, pPos, FurnitureData.DEFAULT);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        if(pLevel instanceof Level level) {
            FurnitureData data = FurnitureData.get(level, pPos);
            return this.shape.move(data.getX(), 0.0, data.getZ());
        }else{
            return this.shape;
        }
    }
}
