package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class NeoForgeModRegistry<T> extends ModRegistry<T> {

    private final DeferredRegister<T> registry;

    public NeoForgeModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
        this.registry = DeferredRegister.create(registryKey, FurnitureMod.MOD_ID);
    }

    @Override
    public void init() {
        this.registry.register(NeoForgeFurnitureMod.INSTANCE.getModBus());
    }

    @Override
    public <E extends T> ModEntry<E> register(String path, Supplier<E> supplier) {
        return new ModEntry<>(this.registry.register(path, supplier));
    }

    @Override
    public void forEach(BiConsumer<String, Supplier<? extends T>> consumer) {
        this.registry.getEntries().forEach(holder -> {
            consumer.accept(holder.getId().getPath(), holder);
        });
    }

}
