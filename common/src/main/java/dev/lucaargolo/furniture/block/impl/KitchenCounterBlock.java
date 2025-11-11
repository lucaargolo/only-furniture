package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.ModBlockTags;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitchenCounterBlock extends FurnitureConnectingBlock implements StoneBlock, WoodBlock {

    public static final BooleanProperty HOLLOW = BooleanProperty.create("hollow");

    private final StoneType stone;
    private final WoodType wood;

    public KitchenCounterBlock(Block base, TagKey<Block> connecting, StoneType stone, WoodType wood) {
        super(base, ModBlockShapes.EMPTY, connecting);
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
        return Shapes.block();
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
