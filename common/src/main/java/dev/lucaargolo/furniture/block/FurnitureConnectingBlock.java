package dev.lucaargolo.furniture.block;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.item.FurnitureConnectingBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FurnitureConnectingBlock extends FurnitureBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public static final BooleanProperty NORTHEAST = BooleanProperty.create("northeast");
    public static final BooleanProperty SOUTHEAST = BooleanProperty.create("southeast");
    public static final BooleanProperty SOUTHWEST = BooleanProperty.create("southwest");
    public static final BooleanProperty NORTHWEST = BooleanProperty.create("northwest");

    public static final BooleanProperty OUTER = BooleanProperty.create("outer");

    private final TagKey<Block> connecting;

    public FurnitureConnectingBlock(Block base, VoxelShape[] shapes, TagKey<Block> connecting) {
        super(base, shapes);
        BlockState state = this.defaultBlockState();
        state = state.setValue(NORTH, false);
        state = state.setValue(EAST, false);
        state = state.setValue(SOUTH, false);
        state = state.setValue(WEST, false);
        if(this.getType().isDiagonalProvider()) {
            state = state.setValue(NORTHEAST, false);
            state = state.setValue(SOUTHEAST, false);
            state = state.setValue(SOUTHWEST, false);
            state = state.setValue(NORTHWEST, false);
        }
        if(this.getType().isOuterProvider()) {
            state = state.setValue(OUTER, false);
        }
        this.registerDefaultState(state);
        this.connecting = connecting;
    }

    public abstract ConnectionType getType();

    public TagKey<Block> getConnecting() {
        return this.connecting;
    }

    @Override
    public void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST);
        if(this.getType().isDiagonalProvider()) {
            builder.add(NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST);
        }
        if(this.getType().isOuterProvider()) {
            builder.add(OUTER);
        }
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if(level instanceof Level)
            clearNeighborsShapeCache((Level) level, pos);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void onPlace(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        super.onPlace(pState, pLevel, pPos, pNewState, pMovedByPiston);
        clearNeighborsShapeCache(pLevel, pPos);
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
        clearNeighborsShapeCache(pLevel, pPos);
    }

    @Override
    protected BlockState computeStateForData(LevelAccessor level, BlockPos pos, BlockState state, FurnitureData data, @Nullable BlockPlaceContext context) {
        Direction facing = this.getType().isDependentOnOriginalRotation() ? Direction.NORTH : data.getFacing(state);

        List<Vec3i> offsets = this.getType().getOffsets();

        Map<Vec3i, FurnitureData> neighbors = new HashMap<>();
        for (Vec3i neighborOffset : offsets) {
            BlockPos neighborPos = pos.offset(neighborOffset);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.is(this.getConnecting())) {
                FurnitureData neighborData = FurnitureData.getOriginal(level, neighborPos);
                neighbors.put(neighborOffset, neighborData);
            }
        }

        Pair<Boolean, Boolean> north = computeNeighbor(level, state, pos, data, neighbors, offsets.get(0), context);
        Pair<Boolean, Boolean> east = computeNeighbor(level, state, pos, data, neighbors, offsets.get(1), context);
        Pair<Boolean, Boolean> south = computeNeighbor(level, state, pos, data, neighbors, offsets.get(2), context);
        Pair<Boolean, Boolean> west = computeNeighbor(level, state, pos, data, neighbors, offsets.get(3), context);

        switch (facing) {
            case NORTH -> {
                state = state.setValue(NORTH, north.getFirst());
                state = state.setValue(EAST, east.getFirst());
                state = state.setValue(SOUTH, south.getFirst());
                state = state.setValue(WEST, west.getFirst());
            }
            case EAST -> {
                state = state.setValue(NORTH, east.getFirst());
                state = state.setValue(EAST, south.getFirst());
                state = state.setValue(SOUTH, west.getFirst());
                state = state.setValue(WEST, north.getFirst());
            }
            case SOUTH -> {
                state = state.setValue(NORTH, south.getFirst());
                state = state.setValue(EAST, west.getFirst());
                state = state.setValue(SOUTH, north.getFirst());
                state = state.setValue(WEST, east.getFirst());
            }
            case WEST -> {
                state = state.setValue(NORTH, west.getFirst());
                state = state.setValue(EAST, north.getFirst());
                state = state.setValue(SOUTH, east.getFirst());
                state = state.setValue(WEST, south.getFirst());
            }
        }

        if(this.getType().isDiagonalProvider()) {
            Pair<Boolean, Boolean> northeast = computeNeighbor(level, state, pos, data, neighbors, offsets.get(4), context);
            Pair<Boolean, Boolean> southeast = computeNeighbor(level, state, pos, data, neighbors, offsets.get(5), context);
            Pair<Boolean, Boolean> southwest = computeNeighbor(level, state, pos, data, neighbors, offsets.get(6), context);
            Pair<Boolean, Boolean> northwest = computeNeighbor(level, state, pos, data, neighbors, offsets.get(7), context);

            switch (facing) {
                case NORTH -> {
                    state = state.setValue(NORTHEAST, northeast.getFirst());
                    state = state.setValue(SOUTHEAST, southeast.getFirst());
                    state = state.setValue(SOUTHWEST, southwest.getFirst());
                    state = state.setValue(NORTHWEST, northwest.getFirst());
                }
                case EAST -> {
                    state = state.setValue(NORTHEAST, southeast.getFirst());
                    state = state.setValue(SOUTHEAST, southwest.getFirst());
                    state = state.setValue(SOUTHWEST, northwest.getFirst());
                    state = state.setValue(NORTHWEST, northeast.getFirst());
                }
                case SOUTH -> {
                    state = state.setValue(NORTHEAST, southwest.getFirst());
                    state = state.setValue(SOUTHEAST, northwest.getFirst());
                    state = state.setValue(SOUTHWEST, northeast.getFirst());
                    state = state.setValue(NORTHWEST, southeast.getFirst());
                }
                case WEST -> {
                    state = state.setValue(NORTHEAST, northwest.getFirst());
                    state = state.setValue(SOUTHEAST, northeast.getFirst());
                    state = state.setValue(SOUTHWEST, southeast.getFirst());
                    state = state.setValue(NORTHWEST, southwest.getFirst());
                }
            }
        }

        if (this.getType().isOuterProvider()) {
            state = state.setValue(OUTER, north.getSecond() || east.getSecond() || south.getSecond() || west.getSecond());
        }

        return state;
    }

    private Pair<Boolean, Boolean> computeNeighbor(LevelAccessor level, BlockState state, BlockPos pos, FurnitureData data, Map<Vec3i, FurnitureData> neighbors, Vec3i offset, @Nullable BlockPlaceContext context) {
        FurnitureData neighborData = neighbors.get(offset);
        return switch (this.getType()) {
            case HORIZONTAL, DIAGONAL -> Pair.of(neighborData != null && data.rotation() % 90f == 0f && neighborData.rotation() % 90 == 0f, false);
            case HORIZONTAL_WITH_SAME_DATA -> Pair.of(neighborData != null && data.rotation() % 90f == 0f && neighborData.equalsIgnoreRotation(data) && neighborData.rotation() % 90f == 0f, false);
            case HORIZONTAL_WITH_SAME_DATA_AND_SAME_ROTATION -> Pair.of(neighborData != null && data.rotation() % 90f == 0f && neighborData.equals(data), false);
            case HORIZONTAL_WITH_SAME_DATA_AND_90_DEGREES_NEIGHBOR -> {
                if(neighborData != null) {
                    float r1 = data.rotation();
                    float r2 = neighborData.rotation();
                    boolean valid = (Math.floorMod((int) (r1 - r2), 360) == 90) || (Math.floorMod((int) (r2 - r1), 360) == 90);
                    yield Pair.of(data.rotation() % 90f == 0f && neighborData.equalsIgnoreRotation(data) && valid, false);
                }else{
                    yield Pair.of(false, false);
                }
            }
            case SOFA, COUNTER -> {
                //If not axis aligned return false
                if(data.rotation() % 90f != 0f) {
                    yield Pair.of(false, false);
                }
                //Computes direction from offset
                Direction facing = Direction.fromYRot(data.rotation() + 180);
                Direction direction = Direction.fromDelta(offset.getX(), offset.getY(), offset.getZ());
                assert direction != null;
                //Calculates the real direction
                Direction realDirection = switch (facing) {
                    case NORTH -> direction;
                    case EAST -> direction.getClockWise();
                    case SOUTH -> direction.getOpposite();
                    case WEST -> direction.getCounterClockWise();
                    default -> throw new IllegalStateException("Unexpected value: " + facing);
                };
                //We will handle the connections always from the EAST and WEST neighbors.
                //If the direction is NORTH, we rotate it to EAST and then do the corner calculation.
                //If the direction is EAST, we keep it as is and then do the regular calculation.
                if(realDirection.getAxis() == Direction.Axis.Z) {
                    direction = direction.getClockWise();
                    offset = direction.getNormal();
                    neighborData = neighbors.get(offset);
                }else{
                    //If neighbor is null
                    if(neighborData == null) {
                        yield Pair.of(false, false);
                    }
                    //If neighbor is on same rotation
                    if(neighborData.equals(data)) {
                        yield Pair.of(true, false);
                    }
                    //If neighbor is connected but on another rotation
                    BlockState neighborState = level.getBlockState(pos.offset(offset));
                    Direction neighborFacing = neighborData.getFacing(neighborState);
                    if(direction == neighborFacing.getOpposite()) {
                        yield Pair.of(neighborState.getValue(NORTH) || neighborState.getValue(SOUTH), false);
                    }else if(direction == neighborFacing) {
                        yield Pair.of(neighborState.getValue(OUTER), false);
                    }
                }
                //If neighbor is not on the same rotation returns false
                if(neighborData == null || !neighborData.equals(data)) {
                    yield Pair.of(false, false);
                }
                //If both axis neighbors on the same rotation returns false
                FurnitureData oppositeNeighborData = neighbors.get(offset.multiply(-1));
                if(oppositeNeighborData != null && oppositeNeighborData.equals(data)) {
                    yield Pair.of(false, false);
                }
                //If both non-axis neighbors are matching or both are not matching returns false
                FurnitureData clockwiseNeighborData = neighbors.get(direction.getClockWise().getNormal());
                BlockState clockwiseNeighborState = level.getBlockState(pos.relative(direction.getClockWise()));
                boolean clockwiseNeighborValid = clockwiseNeighborData != null && clockwiseNeighborData.equalsIgnoreRotation(data) && clockwiseNeighborData.rotation() % 90f == 0f;
                FurnitureData counterClockwiseNeighborData = neighbors.get(direction.getCounterClockWise().getNormal());
                BlockState counterClockwiseNeighborState = level.getBlockState(pos.relative(direction.getCounterClockWise()));
                boolean counterClockwiseNeighborValid = counterClockwiseNeighborData != null && counterClockwiseNeighborData.equalsIgnoreRotation(data) && counterClockwiseNeighborData.rotation() % 90f == 0f;
                if((clockwiseNeighborValid && counterClockwiseNeighborValid) || (!clockwiseNeighborValid && !counterClockwiseNeighborValid)) {
                    yield Pair.of(false, false);
                }
                //Calculates the validity of the counter based on the neighbor rotations (facing)
                BlockPos rotatedPos = pos.relative(clockwiseNeighborValid ? direction.getClockWise() : direction.getCounterClockWise());
                Direction rotatedFacing = clockwiseNeighborValid ? clockwiseNeighborData.getFacing(clockwiseNeighborState) : counterClockwiseNeighborData.getFacing(counterClockwiseNeighborState);
                BlockPos neighborPos = pos.offset(offset);
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
            case FENCE -> {
                if(context != null && context.getPlayer() != null) {
                    Player player = context.getPlayer();
                    BlockPos lastPosition = FurnitureConnectingBlockItem.getLastPosition(player);
                    yield Pair.of(!player.isShiftKeyDown() && pos.offset(offset).equals(lastPosition), false);
                }else{
                    boolean isConnected = isOffsetConnected(state, offset);
                    boolean isConnectionValid = level.getBlockState(pos.offset(offset)).is(this.getConnecting());
                    yield Pair.of(isConnected && isConnectionValid, false);
                }
            }
        };
    }

    public boolean isOffsetConnected(BlockState state, Vec3i offset) {
        if(offset.equals(Direction.NORTH.getNormal())) {
            return state.getValue(NORTH);
        }else if(offset.equals(Direction.EAST.getNormal())) {
            return state.getValue(EAST);
        }else if(offset.equals(Direction.SOUTH.getNormal())) {
            return state.getValue(SOUTH);
        }else if(offset.equals(Direction.WEST.getNormal())) {
            return state.getValue(WEST);
        }else if(this.getType().isDiagonalProvider()) {
            if(offset.equals(Direction.NORTH.getNormal().relative(Direction.EAST))) {
                return state.getValue(NORTHEAST);
            }else if(offset.equals(Direction.SOUTH.getNormal().relative(Direction.EAST))) {
                return state.getValue(SOUTHEAST);
            }else if(offset.equals(Direction.SOUTH.getNormal().relative(Direction.WEST))) {
                return state.getValue(SOUTHWEST);
            }else if(offset.equals(Direction.NORTH.getNormal().relative(Direction.WEST))) {
                return state.getValue(NORTHWEST);
            }
        }
        return false;
    }

    private void clearNeighborsShapeCache(@NotNull Level pLevel, @NotNull BlockPos pPos) {
        List<Vec3i> offsets = this.getType().getOffsets();
        for(Vec3i offset : offsets) {
            FurnitureData.clearShapeCache(pLevel, pPos.offset(offset));
        }
    }

    public enum ConnectionType {
        HORIZONTAL(false, false, false, false),
        HORIZONTAL_WITH_SAME_DATA(false, false, false, false),
        HORIZONTAL_WITH_SAME_DATA_AND_SAME_ROTATION(false, false, false, false),
        HORIZONTAL_WITH_SAME_DATA_AND_90_DEGREES_NEIGHBOR(false, false, false, false),
        DIAGONAL(true, false, false, false),
        COUNTER(false, true, false, false),
        SOFA(false, true, false, false),
        FENCE(true, false, true, true);

        private final boolean diagonalProvider;
        private final boolean outerProvider;
        private final boolean dependentOnLastPosition;
        private final boolean dependentOnOriginalRotation;
        private final List<Vec3i> offsets;

        ConnectionType(boolean diagonalProvider, boolean outerProvider, boolean dependentOnLastPosition, boolean dependentOnOriginalRotation) {
            this.diagonalProvider = diagonalProvider;
            this.outerProvider = outerProvider;
            this.dependentOnLastPosition = dependentOnLastPosition;
            this.dependentOnOriginalRotation = dependentOnOriginalRotation;
            ImmutableList.Builder<Vec3i> builder = ImmutableList.builder();
            builder.add(Direction.NORTH.getNormal());
            builder.add(Direction.EAST.getNormal());
            builder.add(Direction.SOUTH.getNormal());
            builder.add(Direction.WEST.getNormal());
            if(this.isDiagonalProvider()) {
                builder.add(Direction.NORTH.getNormal().relative(Direction.EAST));
                builder.add(Direction.SOUTH.getNormal().relative(Direction.EAST));
                builder.add(Direction.SOUTH.getNormal().relative(Direction.WEST));
                builder.add(Direction.NORTH.getNormal().relative(Direction.WEST));
            }
            this.offsets = builder.build();
        }

        //Provides the NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST properties
        public boolean isDiagonalProvider() {
            return this.diagonalProvider;
        }

        //Provides an OUTER property (used for corner connections)
        public boolean isOuterProvider() {
            return this.outerProvider;
        }

        //Depends on the last position (doesn't connect automatically)
        public boolean isDependentOnLastPosition() {
            return this.dependentOnLastPosition;
        }

        //Ignores the rotation when calculating the neighbors
        public boolean isDependentOnOriginalRotation() {
            return this.dependentOnOriginalRotation;
        }

        public List<Vec3i> getOffsets() {
            return this.offsets;
        }

        @Nullable
        public BooleanProperty getProperty(Vec3i offset) {
            List<Vec3i> offsets = this.getOffsets();
            return switch (offsets.indexOf(offset)) {
                case 0 -> FurnitureConnectingBlock.NORTH;
                case 1 -> FurnitureConnectingBlock.EAST;
                case 2 -> FurnitureConnectingBlock.SOUTH;
                case 3 -> FurnitureConnectingBlock.WEST;
                case 4 -> FurnitureConnectingBlock.NORTHEAST;
                case 5 -> FurnitureConnectingBlock.SOUTHEAST;
                case 6 -> FurnitureConnectingBlock.SOUTHWEST;
                case 7 -> FurnitureConnectingBlock.NORTHWEST;
                default -> null;
            };
        }


    }

}
