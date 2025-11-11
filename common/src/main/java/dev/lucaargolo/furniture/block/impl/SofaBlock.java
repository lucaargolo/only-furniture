package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.base.ColorBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class SofaBlock extends FurnitureConnectingBlock implements ColorBlock {

    private static final Map<Direction, VoxelShape> centerShapes = computeVoxelShapes(ModBlockShapes.SOFA_CENTER);
    private static final Map<Direction, VoxelShape> rightShapes = computeVoxelShapes(ModBlockShapes.SOFA_RIGHT);
    private static final Map<Direction, VoxelShape> leftShapes = computeVoxelShapes(ModBlockShapes.SOFA_LEFT);
    private static final Map<Direction, VoxelShape> innerShapes = computeVoxelShapes(ModBlockShapes.SOFA_INNER);
    private static final Map<Direction, VoxelShape> outerShapes = computeVoxelShapes(ModBlockShapes.SOFA_OUTER);

    private final DyeColor color;

    public SofaBlock(Block base, TagKey<Block> connecting, DyeColor color) {
        super(base, ModBlockShapes.SOFA, connecting);
        this.color = color;
    }

    @Override
    public VoxelShape getShapeForData(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data) {
        boolean north = state.getValue(NORTH);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        boolean east = state.getValue(EAST);
        boolean outer = state.getValue(OUTER);

        Map<Direction, VoxelShape> normalShapes = (east && west) ? centerShapes : east ? rightShapes : west ? leftShapes : shapes;
        Map<Direction, VoxelShape> cornerShapes = outer ? outerShapes : innerShapes;
        Map<Direction, VoxelShape> shapes = (north && !south) || (!north && south) ? cornerShapes : normalShapes;

        Direction facing = Direction.fromYRot(data.getRotation() + 180);

        if ((north && !south && outer) || (!north && south && !outer)) {
            return shapes.get(facing.getCounterClockWise());
        }else{
            return shapes.get(facing);
        }
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.SOFA;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

}
