package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.base.LightBlock;
import dev.lucaargolo.furniture.block.base.impl.MetalFurnitureBlock;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LampPostBlock extends MetalFurnitureBlock implements LightBlock {

    public LampPostBlock(MetalType metal, VoxelShape[] shapes) {
        super(metal, shapes);
    }

}
