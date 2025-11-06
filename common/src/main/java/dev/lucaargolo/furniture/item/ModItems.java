package dev.lucaargolo.furniture.item;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.registry.ModItemRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModItems {

    public static final ModItemRegistry REGISTRY = FurnitureMod.INSTANCE.itemRegistry();

    static {
        ModBlocks.REGISTRY.forEach((entry) -> {
            REGISTRY.register(entry.path(), () -> getBlockItem(entry.get(), new Item.Properties()), entry.getTags()).withTintColor(entry.getTintColor());
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
