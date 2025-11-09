package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.block.base.WoodBlock;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FancyFenceBlock extends FurnitureConnectingBlock {

    private final float size;

    public FancyFenceBlock(Block base, TagKey<Block> connecting, float size) {
        super(base, new VoxelShape[]{
            Block.box(8.0-(size/2.0), 0, 8.0-(size/2.0), 8.0+(size/2.0), 16, 8.0+(size/2.0))
        }, connecting);
        this.size = size;
    }

    public float getSize() {
        return size;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.FANCY_FENCE;
    }

    public static class Hedge extends FancyFenceBlock implements WoodBlock.LeafBlock {

        private final WoodType wood;

        public Hedge(Block base, TagKey<Block> connecting, WoodType wood, float size) {
            super(base, connecting, size);
            this.wood = wood;
        }

        @Override
        public WoodType getWood() {
            return wood;
        }

    }

}
