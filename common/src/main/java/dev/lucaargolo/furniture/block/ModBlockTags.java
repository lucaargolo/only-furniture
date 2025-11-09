package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ModBlockTags {

    public static final TagKey<Block> CONNECTING_TABLE = create("connecting_table");
    public static final TagKey<Block> CONNECTING_COFFEE_TABLE = create("connecting_coffee_table"); ;
    public static final TagKey<Block> CONNECTING_KITCHEN_COUNTER = create("connecting_kitchen_counter");
    public static final TagKey<Block> CONNECTING_HEDGES = create("connecting_hedges");

    public static final TagKey<Block> TOP_FOR_KITCHEN_COUNTER = create("top_for_kitchen_counter");

    private static @NotNull TagKey<Block> create(String path) {
        return TagKey.create(Registries.BLOCK, FurnitureMod.id(path));
    }
}
