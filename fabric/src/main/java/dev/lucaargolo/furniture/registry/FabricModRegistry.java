package dev.lucaargolo.furniture.registry;

import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Supplier;

public class FabricModRegistry<T> extends ModRegistry<T> {

    private final Registry<T> registry;

    @SuppressWarnings("unchecked")
    public FabricModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
        this.registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
    }

    @Override
    public void init() {
        entries.forEach(this::registerEntry);
    }

    private <E extends T> void registerEntry(String path, MinecraftEntry<E> entry) {
        entry.set(Registry.register(registry, entry.key(), entry.get()));
    }

    @Override
    public <E extends T> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, supplier, tags);
        entries.put(path, entry);
        return entry;
    }

    @Override
    public @NotNull Iterator<MinecraftEntry<? extends T>> iterator() {
        return entries.values().iterator();
    }

    @Override
    @Nullable
    public MinecraftEntry<? extends T> get(String path) {
        return entries.get(path);
    }

}