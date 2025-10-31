package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ModBlockTags {

    public static TagKey<Block> CONNECTING_TABLE = create("connecting_table");

    private static @NotNull TagKey<Block> create(String path) {
        return TagKey.create(Registries.BLOCK, FurnitureMod.id(path));
    }
}
