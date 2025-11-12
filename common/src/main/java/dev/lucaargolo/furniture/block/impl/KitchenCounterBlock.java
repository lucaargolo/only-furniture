package dev.lucaargolo.furniture.block.impl;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.ModBlockTags;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import dev.lucaargolo.furniture.utils.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class KitchenCounterBlock extends FurnitureConnectingBlock implements StoneBlock, WoodBlock {

    public static final BooleanProperty HOLLOW = BooleanProperty.create("hollow");

    private static final Map<Pair<Direction, Rotation>, VoxelShape> innerShapes = computeVoxelShapes(ModBlockShapes.KITCHEN_COUNTER_INNER, false);
    private static final Map<Pair<Direction, Rotation>, VoxelShape> outerShapes = computeVoxelShapes(ModBlockShapes.KITCHEN_COUNTER_OUTER, false);

    private final StoneType stone;
    private final WoodType wood;

    public KitchenCounterBlock(Block base, TagKey<Block> connecting, StoneType stone, WoodType wood) {
        super(base, ModBlockShapes.KITCHEN_COUNTER, connecting);
        this.registerDefaultState(this.defaultBlockState().setValue(HOLLOW, false));
        this.stone = stone;
        this.wood = wood;
    }

    @Override
    public void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HOLLOW);
    }

    @Override
    protected BlockState computeStateForData(LevelAccessor level, BlockPos pos, BlockState state, FurnitureData data, @Nullable BlockPlaceContext context) {
        BlockState computed = super.computeStateForData(level, pos, state, data, context);
        BlockPos upPos = pos.above();
        BlockState upState = level.getBlockState(upPos);
        if(upState.is(ModBlockTags.TOP_FOR_KITCHEN_COUNTER)) {
            FurnitureData upData = FurnitureData.get(level, upPos, upState.getValue(LAYER));
            return computed.setValue(HOLLOW, data.equals(upData));
        }else{
            return computed.setValue(HOLLOW, false);
        }
    }

    @Override
    public VoxelShape getShapeForData(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data) {
        boolean north = state.getValue(NORTH);
        boolean south = state.getValue(SOUTH);
        boolean outer = state.getValue(OUTER);

        Map<Pair<Direction, Rotation>, VoxelShape> s = (north && !south) || (!north && south) ? outer ? outerShapes : innerShapes : shapes;
        Direction facing = data.getFacing(state);
        Rotation rotation = data.getRotation();
        if ((north && !south && outer) || (!north && south && !outer)) {
            return s.get(Pair.of(facing.getCounterClockWise(), rotation.getCounterClockWise()));
        }else{
            return s.get(Pair.of(facing, rotation));
        }
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.COUNTER;
    }

    @Override
    public StoneType getStone() {
        return stone;
    }

    @Override
    public WoodType getWood() {
        return wood;
    }

}
