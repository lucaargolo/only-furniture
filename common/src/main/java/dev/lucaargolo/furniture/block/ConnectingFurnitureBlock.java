package dev.lucaargolo.furniture.block;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class ConnectingFurnitureBlock extends FurnitureBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty OUTER = BooleanProperty.create("outer");

    private final TagKey<Block> connecting;

    public ConnectingFurnitureBlock(Block base, VoxelShape[] shapes, TagKey<Block> connecting) {
        super(base, shapes);
        BlockState defaultState = this.defaultBlockState()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false);
        if(this.getType().computesOuter()) {
            defaultState.setValue(OUTER, false);
        }
        this.registerDefaultState(defaultState);
        this.connecting = connecting;
    }

    public abstract ConnectionType getType();

    @Override
    public void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST);
        if(this.getType().computesOuter()) {
            builder.add(OUTER);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context, FurnitureData data, int layer) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return computeStateForData(level, pos, this.defaultBlockState(), data);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        int layer = state.getValue(LAYER);
        FurnitureData data = FurnitureData.get(level, pos, layer);
        return computeStateForData(level, pos, state, data);
    }

    protected BlockState computeStateForData(LevelAccessor level, BlockPos pos, BlockState state, FurnitureData data) {
        Direction facing = Direction.fromYRot(data.getRotation() + 180);
        if (data.getRotation() % 90f == 0f) {

            Map<Direction, FurnitureData> neighbors = new HashMap<>();
            for (Direction neighborDirection : Direction.Plane.HORIZONTAL) {
                BlockPos neighborPos = pos.relative(neighborDirection);
                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.is(this.connecting)) {
                    FurnitureData[] neighborLayers = FurnitureData.get(level, neighborPos);
                    for (FurnitureData neighborData : neighborLayers) {
                        if(data.equalsIgnoreRotation(neighborData) && neighborData.getRotation() % 90f == 0f) {
                            neighbors.put(neighborDirection, neighborData);
                            break;
                        }
                    }
                }
            }



            Pair<Boolean, Boolean> north = computeNeighbor(pos, facing, data, neighbors, Direction.NORTH);
            Pair<Boolean, Boolean> east = computeNeighbor(pos, facing, data, neighbors, Direction.EAST);
            Pair<Boolean, Boolean> south = computeNeighbor(pos, facing, data, neighbors, Direction.SOUTH);
            Pair<Boolean, Boolean> west = computeNeighbor(pos, facing, data, neighbors, Direction.WEST);

            if (this.getType().computesOuter()) {
                state = state.setValue(OUTER, north.getSecond() || east.getSecond() || south.getSecond() || west.getSecond());
            }

            return switch (facing) {
                case NORTH -> state.setValue(NORTH, north.getFirst()).setValue(EAST, east.getFirst()).setValue(SOUTH, south.getFirst()).setValue(WEST, west.getFirst());
                case EAST -> state.setValue(NORTH, east.getFirst()).setValue(EAST, south.getFirst()).setValue(SOUTH, west.getFirst()).setValue(WEST, north.getFirst());
                case SOUTH -> state.setValue(NORTH, south.getFirst()).setValue(EAST, west.getFirst()).setValue(SOUTH, north.getFirst()).setValue(WEST, east.getFirst());
                case WEST -> state.setValue(NORTH, west.getFirst()).setValue(EAST, north.getFirst()).setValue(SOUTH, east.getFirst()).setValue(WEST, south.getFirst());
                default -> throw new IllegalStateException("Unexpected value: " + facing);
            };
        } else {
            return state;
        }
    }

    private Pair<Boolean, Boolean> computeNeighbor(BlockPos pos, Direction facing, FurnitureData data, Map<Direction, FurnitureData> neighbors, Direction direction) {
        FurnitureData neighborData = neighbors.get(direction);
        if(neighborData == null) {
            return Pair.of(false, false);
        }

        return switch (this.getType()) {
            case SAME_DATA -> Pair.of(neighborData.equalsIgnoreRotation(data) && neighborData.getRotation() % 90f == 0f, false);
            case SAME_DATA_SAME_ROTATION -> Pair.of(neighborData.equals(data), false);
            case SAME_DATA_90_DEGREES -> {
                float r1 = data.getRotation();
                float r2 = neighborData.getRotation();
                boolean valid = (Math.floorMod((int)(r1 - r2), 360) == 90) || (Math.floorMod((int)(r2 - r1), 360) == 90);
                yield Pair.of(neighborData.equalsIgnoreRotation(data) && valid, false);
            }
            case COUNTER -> {
                //If neighbor is not on the same rotation returns false
                BlockPos neighborPos = pos.relative(direction);
                if(!neighborData.equals(data)) {
                    yield Pair.of(false, false);
                }
                //If both axis neighbors on the same rotation returns false
                FurnitureData oppositeNeighborData = neighbors.get(direction.getOpposite());
                if(oppositeNeighborData != null && oppositeNeighborData.equals(data)) {
                    yield Pair.of(false, false);
                }
                //If both non-axis neighbors are matching or both are not matching returns false
                FurnitureData clockwiseNeighborData = neighbors.get(direction.getClockWise());
                boolean clockwiseNeighborValid = clockwiseNeighborData != null && clockwiseNeighborData.equalsIgnoreRotation(data);
                FurnitureData counterClockwiseNeighborData = neighbors.get(direction.getCounterClockWise());
                boolean counterClockwiseNeighborValid = counterClockwiseNeighborData != null && counterClockwiseNeighborData.equalsIgnoreRotation(data);
                if((clockwiseNeighborValid && counterClockwiseNeighborValid) || (!clockwiseNeighborValid && !counterClockwiseNeighborValid)) {
                    yield Pair.of(false, false);
                }
                //Calculates the validity of the counter based on the neighbor rotations (facing)
                BlockPos rotatedPos = pos.relative(clockwiseNeighborValid ? direction.getClockWise() : direction.getCounterClockWise());
                Direction rotatedFacing = Direction.fromYRot((clockwiseNeighborValid ? clockwiseNeighborData.getRotation() : counterClockwiseNeighborData.getRotation()) + 180);
                if(neighborPos.relative(facing).equals(rotatedPos.relative(rotatedFacing))) {
                    //Inner counter
                    yield Pair.of(true, false);
                }else if(neighborPos.relative(facing.getOpposite()).equals(rotatedPos.relative(rotatedFacing.getOpposite()))) {
                    //Outer counter
                    yield Pair.of(true, true);
                }else{
                    yield Pair.of(false, false);
                }
            }
        };
    }


    public enum ConnectionType {
        SAME_DATA(false),
        SAME_DATA_SAME_ROTATION(false),
        SAME_DATA_90_DEGREES(false),
        COUNTER(true);

        private final boolean computesOuter;

        ConnectionType(boolean computesOuter) {
            this.computesOuter = computesOuter;
        }

        public boolean computesOuter() {
            return this.computesOuter;
        }
    }

}
