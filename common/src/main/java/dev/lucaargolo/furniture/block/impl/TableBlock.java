package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import dev.lucaargolo.furniture.utils.VoxelShapeUtils;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TableBlock extends FurnitureConnectingBlock implements WoodBlock {

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
            EAST_SHAPES.put((byte) i, VoxelShapeUtils.rotate(combinedShape, Direction.EAST));
            SOUTH_SHAPES.put((byte) i, VoxelShapeUtils.rotate(combinedShape, Direction.SOUTH));
            WEST_SHAPES.put((byte) i, VoxelShapeUtils.rotate(combinedShape, Direction.WEST));
        }
    }

    private final WoodType wood;

    public TableBlock(Block base, VoxelShape[] shapes, TagKey<Block> connecting, WoodType wood) {
        super(base, shapes, connecting);
        this.wood = wood;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.SAME_DATA;
    }

    @Override
    protected VoxelShape getShapeForData(BlockState state, FurnitureData data) {
        Direction facing = Direction.fromYRot(data.getRotation() + 180);
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
