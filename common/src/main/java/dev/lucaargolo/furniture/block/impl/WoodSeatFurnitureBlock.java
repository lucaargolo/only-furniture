package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import dev.lucaargolo.furniture.block.interaction.Interaction;
import dev.lucaargolo.furniture.block.interaction.SeatInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.List;

public class WoodSeatFurnitureBlock extends FurnitureBlock implements WoodBlock {

    private final WoodType wood;
    private final List<? extends Interaction<?>> interactions;

    public WoodSeatFurnitureBlock(Block base, WoodType wood, VoxelShape[] shapes, Vec3... seats) {
        super(base, shapes);
        this.wood = wood;
        this.interactions = Arrays.stream(seats).map(SeatInteraction::new).toList();
    }

    @Override
    public WoodType getWood() {
        return this.wood;
    }

    @Override
    public List<? extends Interaction<?>> getInteractions() {
        return interactions;
    }

}
