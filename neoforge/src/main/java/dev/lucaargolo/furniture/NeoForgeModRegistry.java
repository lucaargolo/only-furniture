package dev.lucaargolo.furniture;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public class NeoForgeModRegistry<T> extends ModRegistry<T> {

    private final Map<String, ModEntry<? extends T>> entries = new HashMap<>();
    private final DeferredRegister<T> registry;

    public NeoForgeModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
        this.registry = DeferredRegister.create(registryKey, FurnitureMod.MOD_ID);
    }

    @Override
    public void init() {
        this.registry.register(NeoForgeFurnitureMod.getModBus());
    }

    @Override
    public <E extends T> ModEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        ModEntry<E> entry = new ModEntry<>(path, this.registry.register(path, supplier), tags);
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