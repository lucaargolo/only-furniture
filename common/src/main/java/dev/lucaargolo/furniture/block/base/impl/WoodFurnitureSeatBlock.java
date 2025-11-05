package dev.lucaargolo.furniture.block.base.impl;

import dev.lucaargolo.furniture.block.FurnitureSeatBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WoodFurnitureSeatBlock extends FurnitureSeatBlock implements WoodBlock {

    private final WoodType wood;

    public WoodFurnitureSeatBlock(Block base, WoodType wood, VoxelShape[] shapes, Vec3... seats) {
        super(base, shapes, seats);
        this.wood = wood;
    }

    public WoodFurnitureSeatBlock(Block base, WoodType wood, VoxelShape[] shapes) {
        this(base, wood, shapes, new Vec3(0.0, 0.375, 0.0));
    }

    @Override
    public WoodType getWood() {
        return wood;
    }

}
