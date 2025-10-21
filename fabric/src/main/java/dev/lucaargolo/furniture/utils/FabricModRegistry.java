package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class FabricModRegistry<T> extends ModRegistry<T>{

    private final Map<String, ModEntry<T>> toRegister = new HashMap<>();
    private final Registry<T> registry;

    public FabricModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
        this.registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
    }

    @Override
    public void init() {
        toRegister.forEach(this::registerEntry);
    }

    private <E extends T> void registerEntry(String name, ModEntry<E> entry) {
        entry.set(Registry.register(registry, FurnitureMod.id(name), entry.get()));
    }

    @Override
    public <E extends T> ModEntry<E> register(String name, Supplier<E> supplier) {
        ModEntry<E> entry = new ModEntry<>(supplier);
        toRegister.put(name, (ModEntry<T>) entry);
        return entry;
    }

}
