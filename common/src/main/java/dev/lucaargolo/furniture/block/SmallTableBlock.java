package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.Shapes;

public class SmallTableBlock extends WoodFurnitureBlock {

    public SmallTableBlock(Block base, WoodType wood) {
        super(base, wood, Shapes.box(0.125,0,0.125,0.875,0.625,0.875), Shapes.box(0,0.625,0,1,0.75,1));
    }

}
