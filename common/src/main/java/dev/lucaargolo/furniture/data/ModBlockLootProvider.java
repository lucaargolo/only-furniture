package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.data.builder.ModBlockLootBuilder;

public class ModBlockLootProvider {

    public static void generate(ModBlockLootBuilder builder) {
        ModBlocks.REGISTRY.getEntries().forEach(entry -> {
            builder.dropSelf(entry.get());
        });
    }


}
