package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FurnitureBlock extends Block {

    public FurnitureBlock(Block base) {
        super(BlockBehaviour.Properties.ofFullCopy(base));
    }

}
