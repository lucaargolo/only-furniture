package dev.lucaargolo.furniture.block.impl;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.base.ColorBlock;
import dev.lucaargolo.furniture.block.interaction.Interaction;
import dev.lucaargolo.furniture.block.interaction.SeatInteraction;
import dev.lucaargolo.furniture.utils.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class SofaBlock extends FurnitureConnectingBlock implements ColorBlock {

    private static final Map<Pair<Direction, Rotation>, VoxelShape> CENTER_SHAPES = computeVoxelShapes(ModBlockShapes.SOFA_CENTER, false);
    private static final Map<Pair<Direction, Rotation>, VoxelShape> RIGHT_SHAPES = computeVoxelShapes(ModBlockShapes.SOFA_RIGHT, false);
    private static final Map<Pair<Direction, Rotation>, VoxelShape> LEFT_SHAPES = computeVoxelShapes(ModBlockShapes.SOFA_LEFT, false);
    private static final Map<Pair<Direction, Rotation>, VoxelShape> INNER_SHAPES = computeVoxelShapes(ModBlockShapes.SOFA_INNER, false);
    private static final Map<Pair<Direction, Rotation>, VoxelShape> OUTER_SHAPES = computeVoxelShapes(ModBlockShapes.SOFA_OUTER, false);

    private final DyeColor color;

    public SofaBlock(Block base, TagKey<Block> connecting, DyeColor color) {
        super(base, ModBlockShapes.SOFA, new Interaction[] {
            new SeatInteraction(Vec3.ZERO)
        }, connecting);
        this.color = color;
    }

    @Override
    public VoxelShape getShapeForFurniture(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data, int layer) {
        boolean north = state.getValue(NORTH);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        boolean east = state.getValue(EAST);
        boolean outer = state.getValue(OUTER);

        Map<Pair<Direction, Rotation>, VoxelShape> normalShapes = (east && west) ? CENTER_SHAPES : east ? RIGHT_SHAPES : west ? LEFT_SHAPES : shapes;
        Map<Pair<Direction, Rotation>, VoxelShape> cornerShapes = outer ? OUTER_SHAPES : INNER_SHAPES;

        Map<Pair<Direction, Rotation>, VoxelShape> s = (north && !south) || (!north && south) ? cornerShapes : normalShapes;
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
        return ConnectionType.SOFA;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

}
