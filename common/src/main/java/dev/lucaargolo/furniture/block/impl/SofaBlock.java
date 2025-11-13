package dev.lucaargolo.furniture.block.impl;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import dev.lucaargolo.furniture.block.ModBlockShapes;
import dev.lucaargolo.furniture.block.base.ColorBlock;
import dev.lucaargolo.furniture.block.base.SeatBlock;
import dev.lucaargolo.furniture.utils.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SofaBlock extends FurnitureConnectingBlock implements ColorBlock, SeatBlock {

    private static final Map<Pair<Direction, Rotation>, VoxelShape> centerShapes = computeVoxelShapes(ModBlockShapes.SOFA_CENTER, false);
    private static final Map<Pair<Direction, Rotation>, VoxelShape> rightShapes = computeVoxelShapes(ModBlockShapes.SOFA_RIGHT, false);
    private static final Map<Pair<Direction, Rotation>, VoxelShape> leftShapes = computeVoxelShapes(ModBlockShapes.SOFA_LEFT, false);
    private static final Map<Pair<Direction, Rotation>, VoxelShape> innerShapes = computeVoxelShapes(ModBlockShapes.SOFA_INNER, false);
    private static final Map<Pair<Direction, Rotation>, VoxelShape> outerShapes = computeVoxelShapes(ModBlockShapes.SOFA_OUTER, false);

    private static final Vec3[] seats = new Vec3[] {
            new Vec3(0.0, 0.375, 0.0)
    };

    private final DyeColor color;

    public SofaBlock(Block base, TagKey<Block> connecting, DyeColor color) {
        super(base, ModBlockShapes.SOFA, connecting);
        this.color = color;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if(tryAndSit(level, pos, player, hitResult)) {
            return InteractionResult.SUCCESS;
        }else{
            return InteractionResult.PASS;
        }
    }

    @Override
    public VoxelShape getShapeForData(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data) {
        boolean north = state.getValue(NORTH);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        boolean east = state.getValue(EAST);
        boolean outer = state.getValue(OUTER);

        Map<Pair<Direction, Rotation>, VoxelShape> normalShapes = (east && west) ? centerShapes : east ? rightShapes : west ? leftShapes : shapes;
        Map<Pair<Direction, Rotation>, VoxelShape> cornerShapes = outer ? outerShapes : innerShapes;

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

    @Override
    public Vec3[] getSeats() {
        return seats;
    }

}
