package dev.lucaargolo.furniture.block.base;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Locale;

public interface StoneBlock {

    StoneType getStone();

    enum StoneType {
        STONE(Blocks.STONE, false),
        SMOOTH_STONE(Blocks.SMOOTH_STONE, true),
        GRANITE(Blocks.GRANITE, false),
        POLISHED_GRANITE(Blocks.POLISHED_GRANITE, true),
        DIORITE(Blocks.DIORITE, false),
        POLISHED_DIORITE(Blocks.POLISHED_DIORITE, true),
        ANDESITE(Blocks.ANDESITE, false),
        POLISHED_ANDESITE(Blocks.POLISHED_ANDESITE, true),
        DEEPSLATE(Blocks.DEEPSLATE, false),
        POLISHED_DEEPSLATE(Blocks.POLISHED_DEEPSLATE, true),
        TUFF(Blocks.TUFF, false),
        POLISHED_TUFF(Blocks.POLISHED_TUFF, true),
        SANDSTONE(Blocks.SANDSTONE, false),
        SMOOTH_SANDSTONE(Blocks.SMOOTH_SANDSTONE, true),
//        RED_SANDSTONE(Blocks.RED_SANDSTONE, false),
//        SMOOTH_RED_SANDSTONE(Blocks.SMOOTH_RED_SANDSTONE, true),
        BLACKSTONE(Blocks.BLACKSTONE, false),
        POLISHED_BLACKSTONE(Blocks.POLISHED_BLACKSTONE, true),
        SMOOTH_QUARTZ(Blocks.SMOOTH_QUARTZ, false),
        QUARTZ_BLOCK(Blocks.QUARTZ_BLOCK, true);

        private final Block base;
        private final boolean polished;

        StoneType(Block base, boolean polished) {
            this.base = base;
            this.polished = polished;
        }

        public String getPath() {
            return name().toLowerCase(Locale.US);
        }

        public Block getBase() {
            return base;
        }

        public boolean isPolished() {
            return polished;
        }


    }

}
