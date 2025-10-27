package dev.lucaargolo.furniture.data;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.item.ModItems;
import dev.lucaargolo.furniture.utils.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModTagsProvider<T> extends TagsProvider<T> {

    private final ModRegistry<T> registry;

    protected ModTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper, ModRegistry<T> registry) {
        super(output, registry.getRegistryKey(), lookupProvider, FurnitureMod.MOD_ID, existingFileHelper);
        this.registry = registry;
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.registry.forEach((path, entry) -> {
            Arrays.stream(entry.getTags()).map(t -> t.cast(registryKey)).filter(Optional::isPresent).map(Optional::get).forEach(tag -> {
                getOrCreateRawBuilder(tag).addElement(FurnitureMod.id(path)).replace(false);
            });
        });
    }

    public static ModTagsProvider<Block> block(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        return new ModTagsProvider<>(output, lookupProvider, existingFileHelper, ModBlocks.BLOCKS);
    }

    public static ModTagsProvider<Item> item(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        return new ModTagsProvider<>(output, lookupProvider, existingFileHelper, ModItems.ITEMS);
    }

}
