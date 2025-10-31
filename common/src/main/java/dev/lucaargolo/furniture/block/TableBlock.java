package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class TableBlock extends FurnitureBlock implements WoodBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    private final WoodType wood;

    public TableBlock(Block base, WoodType wood) {
        super(base);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
        );
        this.wood = wood;
    }

    @Override
    public void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context, FurnitureData data, int layer) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return computeState(level, pos, this.defaultBlockState(), data);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        int layer = state.getValue(LAYER);
        FurnitureData data = FurnitureData.get(level, pos, layer);
        return computeState(level, pos, state, data);
    }

    private BlockState computeState(LevelAccessor level, BlockPos pos, BlockState state, FurnitureData data) {
        if(data.getRotation() % 90f == 0f) {
            boolean north = false;
            boolean east = false;
            boolean south = false;
            boolean west = false;

            for(Direction neighborDirection : Direction.Plane.HORIZONTAL) {
                BlockPos neighborPos = pos.relative(neighborDirection);
                BlockState neighborState = level.getBlockState(neighborPos);

                if(neighborState.getBlock() == this) {
                    FurnitureData[] neighborLayers = FurnitureData.get(level, neighborPos);
                    for(FurnitureData neighborData : neighborLayers) {
                        if(neighborData.equals(data)) {
                            north = north || neighborDirection == Direction.NORTH;
                            east = east || neighborDirection == Direction.EAST;
                            south = south || neighborDirection == Direction.SOUTH;
                            west = west || neighborDirection == Direction.WEST;
                            break;
                        }
                    }
                }
            }

            Direction facing = Direction.fromYRot(data.getRotation() + 180);
            return switch (facing) {
                case NORTH -> state.setValue(NORTH, north).setValue(EAST, east).setValue(SOUTH, south).setValue(WEST, west);
                case EAST -> state.setValue(NORTH, west).setValue(EAST, north).setValue(SOUTH, east).setValue(WEST, south);
                case SOUTH -> state.setValue(NORTH, south).setValue(EAST, west).setValue(SOUTH, north).setValue(WEST, east);
                case WEST -> state.setValue(NORTH, east).setValue(EAST, south).setValue(SOUTH, west).setValue(WEST, north);
                default -> throw new IllegalStateException("Unexpected value: " + facing);
            };
        } else {
            return state;
        }
    }

    @Override
    protected VoxelShape getShapes(BlockState state, Direction facing) {
        return Shapes.block();
    }

    @Override
    public WoodType getWood() {
        return wood;
    }

}
