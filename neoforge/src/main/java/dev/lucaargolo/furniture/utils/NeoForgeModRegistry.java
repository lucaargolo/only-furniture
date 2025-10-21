package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;

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
    public <E extends T> ModEntry<E> register(String name, Supplier<E> supplier) {
        return new ModEntry<E>(this.registry.register(name, supplier));
    }

}
