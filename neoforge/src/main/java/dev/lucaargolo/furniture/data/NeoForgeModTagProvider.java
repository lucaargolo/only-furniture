package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.ModRegistry;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class NeoForgeModTagProvider<T> extends TagsProvider<T> implements ModTagProvider<T> {

    private final ModRegistry<T> registry;

    protected NeoForgeModTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper, ModRegistry<T> registry) {
        super(output, registry.getRegistryKey(), lookupProvider, FurnitureMod.MOD_ID, existingFileHelper);
        this.registry = registry;
    }

    @Override
    public ModTagBuilder<T> getOrCreateModTagBuilder(TagKey<T> tag) {
        return new NeoForgeModTagBuilder<>(registryKey, this.getOrCreateRawBuilder(tag));
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        this.addTags(registry);
    }

    public static NeoForgeModTagProvider<Block> block(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        return new NeoForgeModTagProvider<>(output, lookupProvider, existingFileHelper, ModBlocks.BLOCKS);
    }

    public static NeoForgeModTagProvider<Item> item(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        return new NeoForgeModTagProvider<>(output, lookupProvider, existingFileHelper, ModItems.ITEMS);
    }

}
