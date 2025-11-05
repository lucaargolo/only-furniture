package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.ModBlockTags;
import dev.lucaargolo.furniture.block.base.impl.MetalFurnitureBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class KitchenSinkBlock extends MetalFurnitureBlock {

    public static final BooleanProperty DROPPED = BooleanProperty.create("dropped");
    private final Map<Direction, VoxelShape> droppedShapes;

    public KitchenSinkBlock(MetalType metal, WeatheringCopper.WeatherState state, VoxelShape[] ignored) {
        super(metal, state, ModBlockShapes.KITCHEN_SINK);
        this.droppedShapes = computeVoxelShapes(ModBlockShapes.KITCHEN_SINK_DROPPED);
        this.registerDefaultState(this.defaultBlockState().setValue(DROPPED, false));
    }

    public KitchenSinkBlock(MetalType metal) {
        super(metal, ModBlockShapes.KITCHEN_SINK);
        this.droppedShapes = computeVoxelShapes(ModBlockShapes.KITCHEN_SINK_DROPPED);
        this.registerDefaultState(this.defaultBlockState().setValue(DROPPED, false));
    }

    @Override
    public void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DROPPED);
    }

    @Override
    protected BlockState computeStateForData(LevelAccessor level, BlockPos pos, BlockState state, FurnitureData data) {
        BlockState computed = super.computeStateForData(level, pos, state, data);
        BlockPos downPos = pos.below();
        BlockState downState = level.getBlockState(downPos);
        if(downState.is(ModBlockTags.CONNECTING_KITCHEN_COUNTER) && !downState.getValue(FurnitureConnectingBlock.OUTER) && !downState.getValue(FurnitureConnectingBlock.NORTH) && !downState.getValue(FurnitureConnectingBlock.EAST) && !downState.getValue(FurnitureConnectingBlock.SOUTH) && !downState.getValue(FurnitureConnectingBlock.WEST)) {
            FurnitureData downData = FurnitureData.get(level, downPos, downState.getValue(LAYER));
            return computed.setValue(DROPPED, data.equals(downData));
        }else{
            return computed.setValue(DROPPED, false);
        }
    }

    @Override
    protected VoxelShape getShapeForData(BlockState state, FurnitureData data) {
        Direction facing = Direction.fromYRot(data.getRotation() + 180);
        return state.getValue(DROPPED) ? this.droppedShapes.get(facing) : super.getShapeForData(state, data);
    }

    public static class Weathering extends KitchenSinkBlock implements WeatheringCopper {

        public Weathering(MetalType metal, WeatherState state, VoxelShape[] shapes) {
            super(metal, state, shapes);
        }

        @Override
        protected void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
            this.changeOverTime(state, level, pos, random);
        }

        @Override
        public @NotNull WeatherState getAge() {
            return super.getAge();
        }

    }


}
