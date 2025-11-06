package dev.lucaargolo.furniture.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class MinecraftRegistry<T, M extends MinecraftEntry<? extends T>> implements Iterable<M> {

    protected final ResourceKey<Registry<T>> registryKey;
    protected final Map<String, M> entries = new LinkedHashMap<>();

    protected int id = 0;

    protected MinecraftRegistry(ResourceKey<Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public abstract void init();

    @Nullable
    public abstract M get(String path);

    public abstract <E extends T> M register(String path, Supplier<E> supplier, TagKey<?>... tags);

    protected abstract <E extends T> M entry(String path, Supplier<E> supplier, TagKey<?>... tags);

    public ResourceKey<Registry<T>> getRegistryKey() {
        return registryKey;
    }

}
