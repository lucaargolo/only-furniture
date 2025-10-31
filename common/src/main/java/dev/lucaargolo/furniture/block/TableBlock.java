package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.utils.FurnitureData;
import dev.lucaargolo.furniture.utils.VoxelShapeUtils;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
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
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class TableBlock extends FurnitureBlock implements WoodBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    private static final VoxelShape CENTER = Block.box(0, 12, 0, 16, 16, 16);

    private static final VoxelShape FEET_NORTH_EAST = Block.box(11, 0, 2, 14, 12, 5);
    private static final VoxelShape FEET_SOUTH_EAST = VoxelShapeUtils.rotate(FEET_NORTH_EAST, Direction.EAST);
    private static final VoxelShape FEET_SOUTH_WEST = VoxelShapeUtils.rotate(FEET_NORTH_EAST, Direction.SOUTH);
    private static final VoxelShape FEET_NORTH_WEST = VoxelShapeUtils.rotate(FEET_NORTH_EAST, Direction.WEST);

    private static final Byte2ObjectMap<VoxelShape> NORTH_SHAPES = new Byte2ObjectOpenHashMap<>();
    private static final Byte2ObjectMap<VoxelShape> EAST_SHAPES = new Byte2ObjectOpenHashMap<>();
    private static final Byte2ObjectMap<VoxelShape> SOUTH_SHAPES = new Byte2ObjectOpenHashMap<>();
    private static final Byte2ObjectMap<VoxelShape> WEST_SHAPES = new Byte2ObjectOpenHashMap<>();

    static {
        for (int i = 0; i < 16; i++) {
            boolean north = (i & 1) != 0;
            boolean east  = (i & 2) != 0;
            boolean south = (i & 4) != 0;
            boolean west  = (i & 8) != 0;

            VoxelShape combinedShape = CENTER;

            if (!north && !east)
                combinedShape = Shapes.join(combinedShape, FEET_NORTH_EAST, BooleanOp.OR);
            if (!south && !east)
                combinedShape = Shapes.join(combinedShape, FEET_SOUTH_EAST, BooleanOp.OR);
            if (!south && !west)
                combinedShape = Shapes.join(combinedShape, FEET_SOUTH_WEST, BooleanOp.OR);
            if (!north && !west)
                combinedShape = Shapes.join(combinedShape, FEET_NORTH_WEST, BooleanOp.OR);

            NORTH_SHAPES.put((byte) i, combinedShape);
            EAST_SHAPES.put((byte) i, VoxelShapeUtils.rotate(combinedShape, Direction.WEST));
            SOUTH_SHAPES.put((byte) i, VoxelShapeUtils.rotate(combinedShape, Direction.SOUTH));
            WEST_SHAPES.put((byte) i, VoxelShapeUtils.rotate(combinedShape, Direction.EAST));
        }
    }

    private final WoodType wood;
    private final TagKey<Block> connecting;


    public TableBlock(Block base, WoodType wood, TagKey<Block> connecting) {
        super(base);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
        );
        this.wood = wood;
        this.connecting = connecting;
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
        super.updateShape(state, direction, neighborState, level, pos, neighborPos);
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

                if(neighborState.is(this.connecting)) {
                    FurnitureData[] neighborLayers = FurnitureData.get(level, neighborPos);
                    for(FurnitureData neighborData : neighborLayers) {
                        if(neighborData.equalsIgnoreRotation(data) && neighborData.getRotation() % 90f == 0f) {
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
        int key = 0;
        if (state.getValue(NORTH)) key |= 1;
        if (state.getValue(EAST)) key |= 2;
        if (state.getValue(SOUTH)) key |= 4;
        if (state.getValue(WEST)) key |= 8;
        return switch (facing) {
            case NORTH -> NORTH_SHAPES.get((byte) key);
            case EAST -> EAST_SHAPES.get((byte) key);
            case SOUTH -> SOUTH_SHAPES.get((byte) key);
            case WEST -> WEST_SHAPES.get((byte) key);
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        };
    }

    @Override
    public WoodType getWood() {
        return wood;
    }

}
