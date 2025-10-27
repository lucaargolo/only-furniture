package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WoodFurnitureBlock extends FurnitureBlock{

    private final WoodType wood;

    public WoodFurnitureBlock(Block base, WoodType wood, VoxelShape... shapes) {
        super(base, shapes);
        this.wood = wood;
    }

    public WoodType getWood() {
        return wood;
    }
}
