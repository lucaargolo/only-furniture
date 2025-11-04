package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StoneWoodFurnitureBlock extends WoodFurnitureBlock implements StoneBlock {

    private final StoneType stone;

    public StoneWoodFurnitureBlock(Block base, StoneType stone, WoodType wood, VoxelShape[] shapes) {
        super(base, wood, shapes);
        this.stone = stone;
    }

    @Override
    public StoneType getStone() {
        return stone;
    }

}
