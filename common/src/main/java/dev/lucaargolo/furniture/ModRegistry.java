package dev.lucaargolo.furniture;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class ModRegistry<T> implements Iterable<ModRegistry.ModEntry<? extends T>> {

    protected final ResourceKey<Registry<T>> registryKey;

    public ModRegistry(ResourceKey<Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public abstract void init();

    public abstract <E extends T> ModEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags);

    @Nullable
    public abstract ModEntry<? extends T> get(String path);

    public ResourceKey<Registry<T>> getRegistryKey() {
        return registryKey;
    }

    public static class ModEntry<E> implements Supplier<E> {

        private final String path;
        private final Supplier<E> supplier;
        private final TagKey<?>[] tags;

        private boolean supplied = false;
        private E value;

        public ModEntry(String path, Supplier<E> supplier, TagKey<?>... tags) {
            this.path = path;
            this.supplier = supplier;
            this.tags = tags;
        }

        public TagKey<?>[] getTags() {
            return tags;
        }

        @Override
        public E get() {
            return supplied ? value : supplier.get();
        }

        public void set(E value) {
            this.supplied = true;
            this.value = value;
        }

        public String path() {
            return this.path;
        }

        public ResourceLocation key() {
            return FurnitureMod.id(path);
        }

    }

}
