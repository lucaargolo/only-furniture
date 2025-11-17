package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.block.interaction.SeatInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class WoodSeatBlock extends FurnitureBlock implements WoodBlock {

    private final WoodType wood;

    public WoodSeatBlock(Block base, WoodType wood, VoxelShape[] shapes, Vec3... seats) {
        super(base, shapes, Arrays.stream(seats).map(SeatInteraction::new).toArray(SeatInteraction[]::new));
        this.wood = wood;
    }

    @Override
    public WoodType getWood() {
        return this.wood;
    }

}
