package dev.lucaargolo.furniture;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class FabricModRegistry<T> extends ModRegistry<T> {

    private final Map<String, ModEntry<? extends T>> entries = new HashMap<>();
    private final Registry<T> registry;
    private int id = 0;

    public FabricModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
        this.registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
    }

    @Override
    public void init() {
        entries.forEach(this::registerEntry);
    }

    private <E extends T> void registerEntry(String path, ModEntry<E> entry) {
        entry.set(Registry.register(registry, entry.key(), entry.get()));
    }

    @Override
    public <E extends T> ModEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        ModEntry<E> entry = new ModEntry<>(id++, path, supplier, tags);
        entries.put(path, entry);
        return entry;
    }

    @Override
    public @NotNull Iterator<ModEntry<? extends T>> iterator() {
        return entries.values().iterator();
    }

    @Override
    @Nullable
    public ModEntry<? extends T> get(String path) {
        return entries.get(path);
    }

}