package dev.lucaargolo.furniture.registry;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeModRegistry<T> extends ModRegistry<T> {

    private final DeferredRegister<T> registry;

    public NeoForgeModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
        this.registry = DeferredRegister.create(registryKey, FurnitureMod.MOD_ID);
        if (registryKey.location().getNamespace().equals(FurnitureMod.MOD_ID)) {
            this.registry.makeRegistry(builder -> {
                builder.sync(true);
            });
        }
    }

    @Override
    public void init() {
        this.registry.register(NeoForgeFurnitureMod.getModBus());
    }

    @Override
    public <E extends T> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, this.registry.register(path, supplier), tags);
        entries.put(path, entry);
        return entry;
    }

}