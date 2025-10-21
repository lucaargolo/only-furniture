package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class FabricModRegistry<T> extends ModRegistry<T>{

    private final Map<String, ModEntry<T>> entries = new HashMap<>();
    private final Registry<T> registry;

    public FabricModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
        this.registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
    }

    @Override
    public void init() {
        entries.forEach(this::registerEntry);
    }

    private <E extends T> void registerEntry(String path, ModEntry<E> entry) {
        entry.set(Registry.register(registry, FurnitureMod.id(path), entry.get()));
    }

    @Override
    public <E extends T> ModEntry<E> register(String path, Supplier<E> supplier) {
        ModEntry<E> entry = new ModEntry<>(supplier);
        entries.put(path, (ModEntry<T>) entry);
        return entry;
    }

    @Override
    public void forEach(BiConsumer<String, Supplier<? extends T>> consumer) {
        entries.forEach(consumer);
    }

}
