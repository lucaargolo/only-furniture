package dev.lucaargolo.furniture.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class ModRegistry<T> {

    protected final ResourceKey<Registry<T>> registryKey;

    public ModRegistry(ResourceKey<Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public abstract void init();

    public abstract <E extends T> ModEntry<E> register(String path, Supplier<E> supplier);

    public abstract void forEach(BiConsumer<String, Supplier<? extends T>> consumer);

    public static class ModEntry<E> implements Supplier<E> {

        private final Supplier<E> supplier;

        private boolean supplied = false;
        private E value;

        public ModEntry(Supplier<E> supplier) {
            this.supplier = supplier;
        }

        @Override
        public E get() {
            return supplied ? value : supplier.get();
        }

        public void set(E value) {
            this.supplied = true;
            this.value = value;
        }

    }

}
