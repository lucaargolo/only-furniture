package dev.lucaargolo.furniture.item;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.utils.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModItems {

    public static final ModRegistry<Item> ITEMS = FurnitureMod.INSTANCE.registry(Registries.ITEM);

    static {
        ModBlocks.BLOCKS.forEach((path, block) -> {
            ITEMS.register(path, () -> getBlockItem(block.get(), new Item.Properties()), block.getTags());
        });
    }

    private static BlockItem getBlockItem(Block block, Item.Properties properties) {
        if(block instanceof FurnitureBlock furnitureBlock) {
            return new FurnitureBlockItem(furnitureBlock, properties);
        }else{
            return new BlockItem(block, properties);
        }
    }

}
