package dev.lucaargolo.furniture.block.base.impl;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WoodFurnitureBlock extends FurnitureBlock implements WoodBlock {

    private final WoodType wood;

    public WoodFurnitureBlock(Block base, WoodType wood, VoxelShape[] shapes) {
        super(base, shapes);
        this.wood = wood;
    }

    @Override
    public WoodType getWood() {
        return wood;
    }
}
