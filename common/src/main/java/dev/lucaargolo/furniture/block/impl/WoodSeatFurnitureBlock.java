package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.base.SeatBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class WoodSeatFurnitureBlock extends FurnitureBlock implements WoodBlock, SeatBlock {

    private final WoodType wood;
    private final Vec3[] seats;

    public WoodSeatFurnitureBlock(Block base, WoodType wood, VoxelShape[] shapes, Vec3... seats) {
        super(base, shapes);
        this.wood = wood;
        this.seats = seats;
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
    public WoodType getWood() {
        return this.wood;
    }

    @Override
    public Vec3[] getSeats() {
        return this.seats;
    }
}
