package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.base.LightBlock;
import dev.lucaargolo.furniture.block.base.impl.MetalFurnitureBlock;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MetalLampBlock extends MetalFurnitureBlock implements LightBlock {

    public MetalLampBlock(MetalType metal, VoxelShape[] shapes) {
        super(metal, shapes);
    }

    public static class Wall extends MetalLampBlock {

        public Wall(MetalType metal, VoxelShape[] shapes) {
            super(metal, shapes);
        }

        @Override
        public boolean isWallBlock() {
            return true;
        }

    }

}
