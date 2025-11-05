package dev.lucaargolo.furniture.block.base.impl;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.base.StoneBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StoneFurnitureBlock extends FurnitureBlock implements StoneBlock {

    private final StoneType stone;

    public StoneFurnitureBlock(Block base, StoneBlock.StoneType stone, VoxelShape[] shapes) {
        super(base, shapes);
        this.stone = stone;
    }

    public StoneFurnitureBlock(StoneBlock.StoneType stone, VoxelShape[] shapes) {
        super(stone.getBase(), shapes);
        this.stone = stone;
    }

    @Override
    public StoneType getStone() {
        return stone;
    }

}
