package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.block.ModBlocks;

public interface ModBlockLootProvider {

    static void generate(ModBlockLootBuilder builder) {
        ModBlocks.BLOCKS.forEach(entry -> {
            builder.dropSelf(entry.get());
        });
    }


}
