package dev.lucaargolo.furniture.block.impl;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.utils.Rotation;
import dev.lucaargolo.furniture.utils.shape.ShapeUtils;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class TableBlock extends FurnitureConnectingBlock implements WoodBlock {

    private final Map<Direction, Byte2ObjectMap<VoxelShape>> shapes;
    private final WoodType wood;
    private final boolean simple;

    public TableBlock(Block base, TagKey<Block> connecting, WoodType wood, VoxelShape[] footShapes, VoxelShape[] centerShapes, VoxelShape[] sideShapes) {
        super(base, ModBlockShapes.EMPTY, connecting);
        this.shapes = computeVoxelShapes(footShapes, centerShapes, sideShapes);
        this.wood = wood;
        this.simple = sideShapes.length == 0;
    }

    public boolean isSimple() {
        return simple;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.HORIZONTAL_WITH_SAME_DATA;
    }

    @Override
    public VoxelShape getShapeForFurniture(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data, int layer) {
        Direction facing = data.getFacing(state);
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

    public static Map<Direction, Byte2ObjectMap<VoxelShape>> computeVoxelShapes(VoxelShape[] footShapes, VoxelShape[] centerShapes, VoxelShape[] sideShapes) {
        Map<Pair<Direction, Rotation>, VoxelShape> footShapeMap = FurnitureBlock.computeVoxelShapes(footShapes, false);
        Map<Pair<Direction, Rotation>, VoxelShape> centerShapeMap = FurnitureBlock.computeVoxelShapes(centerShapes, false);
        Map<Pair<Direction, Rotation>, VoxelShape> sideShapeMap = FurnitureBlock.computeVoxelShapes(sideShapes, false);

        Byte2ObjectMap<VoxelShape> northShapes = new Byte2ObjectOpenHashMap<>();
        Byte2ObjectMap<VoxelShape> eastShapes = new Byte2ObjectOpenHashMap<>();
        Byte2ObjectMap<VoxelShape> southShapes = new Byte2ObjectOpenHashMap<>();
        Byte2ObjectMap<VoxelShape> westShapes = new Byte2ObjectOpenHashMap<>();
        for (int i = 0; i < 16; i++) {
            boolean north = (i & 1) != 0;
            boolean east  = (i & 2) != 0;
            boolean south = (i & 4) != 0;
            boolean west  = (i & 8) != 0;

            VoxelShape combinedShape = centerShapeMap.get(Pair.of(Direction.NORTH, Rotation.R0));

            if(east)
                combinedShape = Shapes.or(combinedShape, sideShapeMap.get(Pair.of(Direction.NORTH, Rotation.R0)));
            if(south)
                combinedShape = Shapes.or(combinedShape, sideShapeMap.get(Pair.of(Direction.EAST, Rotation.R90)));
            if(west)
                combinedShape = Shapes.or(combinedShape, sideShapeMap.get(Pair.of(Direction.SOUTH, Rotation.R180)));
            if(north)
                combinedShape = Shapes.or(combinedShape, sideShapeMap.get(Pair.of(Direction.WEST, Rotation.R270)));

            if (!north && !east)
                combinedShape = Shapes.or(combinedShape, footShapeMap.get(Pair.of(Direction.NORTH, Rotation.R0)));
            if (!east && !south)
                combinedShape = Shapes.or(combinedShape, footShapeMap.get(Pair.of(Direction.EAST, Rotation.R90)));
            if (!south && !west)
                combinedShape = Shapes.or(combinedShape, footShapeMap.get(Pair.of(Direction.SOUTH, Rotation.R180)));
            if (!west && !north)
                combinedShape = Shapes.or(combinedShape, footShapeMap.get(Pair.of(Direction.WEST, Rotation.R270)));

            northShapes.put((byte) i, combinedShape);
            eastShapes.put((byte) i, ShapeUtils.rotateY(combinedShape, Direction.EAST));
            southShapes.put((byte) i, ShapeUtils.rotateY(combinedShape, Direction.SOUTH));
            westShapes.put((byte) i, ShapeUtils.rotateY(combinedShape, Direction.WEST));
        }

        ImmutableMap.Builder<Direction, Byte2ObjectMap<VoxelShape>> builder = ImmutableMap.builder();
        builder.put(Direction.NORTH, northShapes);
        builder.put(Direction.EAST, eastShapes);
        builder.put(Direction.SOUTH, southShapes);
        builder.put(Direction.WEST, westShapes);
        return builder.build();
    }

}
