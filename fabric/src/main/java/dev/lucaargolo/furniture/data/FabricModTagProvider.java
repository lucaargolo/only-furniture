package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.ModRegistry;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class FabricModTagProvider<T> extends FabricTagProvider<T> implements ModTagProvider<T> {

    private final ModRegistry<T> registry;

    public FabricModTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, ModRegistry<T> registry) {
        super(output, registry.getRegistryKey(), registriesFuture);
        this.registry = registry;
    }

    @Override
    public ModTagBuilder<T> getOrCreateModTagBuilder(TagKey<T> tag) {
        return new FabricModTagBuilder<>(this.registryKey, this.getOrCreateTagBuilder(tag));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.addTags(registry);
    }

    public static FabricModTagProvider<Block> block(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        return new FabricModTagProvider<>(output, registriesFuture, ModBlocks.BLOCKS);
    }

    public static FabricModTagProvider<Item> item(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        return new FabricModTagProvider<>(output, registriesFuture, ModItems.ITEMS);
    }

}
