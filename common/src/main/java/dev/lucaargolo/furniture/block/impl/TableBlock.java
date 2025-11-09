package dev.lucaargolo.furniture.block.impl;

import com.google.common.collect.ImmutableMap;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
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

import java.util.Map;

public class TableBlock extends FurnitureConnectingBlock implements WoodBlock {

    private final Map<Direction, Byte2ObjectMap<VoxelShape>> shapes;
    private final WoodType wood;
    private final boolean simple;

    public TableBlock(Block base, TagKey<Block> connecting, WoodType wood, VoxelShape[] centerShapes, VoxelShape[] footShapes, boolean simple) {
        super(base, ModBlockShapes.EMPTY, connecting);
        this.shapes = computeVoxelShapes(centerShapes, footShapes);
        this.wood = wood;
        this.simple = simple;
    }

    public boolean isSimple() {
        return simple;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.HORIZONTAL_WITH_SAME_DATA;
    }

    @Override
    protected VoxelShape getShapeForData(BlockState state, FurnitureData data) {
        Direction facing = Direction.fromYRot(data.getRotation() + 180);
        int key = 0;
        if (state.getValue(NORTH)) key |= 1;
        if (state.getValue(EAST)) key |= 2;
        if (state.getValue(SOUTH)) key |= 4;
        if (state.getValue(WEST)) key |= 8;
        return this.shapes.get(facing).get((byte) key);
    }

    @Override
    public WoodType getWood() {
        return wood;
    }

    public static Map<Direction, Byte2ObjectMap<VoxelShape>> computeVoxelShapes(VoxelShape[] centerShapes, VoxelShape[] footShapes) {
        Map<Direction, VoxelShape> centerShapeMap = FurnitureBlock.computeVoxelShapes(centerShapes);
        Map<Direction, VoxelShape> footShapeMap = FurnitureBlock.computeVoxelShapes(footShapes);

        Byte2ObjectMap<VoxelShape> northShapes = new Byte2ObjectOpenHashMap<>();
        Byte2ObjectMap<VoxelShape> eastShapes = new Byte2ObjectOpenHashMap<>();
        Byte2ObjectMap<VoxelShape> southShapes = new Byte2ObjectOpenHashMap<>();
        Byte2ObjectMap<VoxelShape> westShapes = new Byte2ObjectOpenHashMap<>();
        for (int i = 0; i < 16; i++) {
            boolean north = (i & 1) != 0;
            boolean east  = (i & 2) != 0;
            boolean south = (i & 4) != 0;
            boolean west  = (i & 8) != 0;

            VoxelShape combinedShape = centerShapeMap.get(Direction.NORTH);
            if (!north && !east)
                combinedShape = Shapes.join(combinedShape, footShapeMap.get(Direction.NORTH), BooleanOp.OR);
            if (!east && !south)
                combinedShape = Shapes.join(combinedShape, footShapeMap.get(Direction.EAST), BooleanOp.OR);
            if (!south && !west)
                combinedShape = Shapes.join(combinedShape, footShapeMap.get(Direction.SOUTH), BooleanOp.OR);
            if (!west && !north)
                combinedShape = Shapes.join(combinedShape, footShapeMap.get(Direction.WEST), BooleanOp.OR);

            northShapes.put((byte) i, combinedShape);
            eastShapes.put((byte) i, VoxelShapeUtils.rotate(combinedShape, Direction.EAST));
            southShapes.put((byte) i, VoxelShapeUtils.rotate(combinedShape, Direction.SOUTH));
            westShapes.put((byte) i, VoxelShapeUtils.rotate(combinedShape, Direction.WEST));
        }

        ImmutableMap.Builder<Direction, Byte2ObjectMap<VoxelShape>> builder = ImmutableMap.builder();
        builder.put(Direction.NORTH, northShapes);
        builder.put(Direction.EAST, eastShapes);
        builder.put(Direction.SOUTH, southShapes);
        builder.put(Direction.WEST, westShapes);
        return builder.build();
    }

}
