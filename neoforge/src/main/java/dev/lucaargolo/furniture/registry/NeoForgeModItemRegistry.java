package dev.lucaargolo.furniture.registry;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Supplier;

public class NeoForgeModItemRegistry extends ModItemRegistry {

    private final DeferredRegister.Items registry = DeferredRegister.createItems(FurnitureMod.MOD_ID);

    @Override
    public void init() {
        this.registry.register(NeoForgeFurnitureMod.getModBus());
    }

    @Override
    @Nullable
    public ItemEntry<?> get(String path) {
        return entries.get(path);
    }

    @Override
    public <E extends Item> ItemEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        ItemEntry<E> entry = this.entry(path, this.registry.register(path, supplier), tags);
        entries.put(path, entry);
        return entry;
    }

    @Override
    public @NotNull Iterator<ItemEntry<?>> iterator() {
        return entries.values().iterator();
    }

}