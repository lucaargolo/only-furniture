package dev.lucaargolo.furniture.registry;

import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class FabricModMenuTypeRegistry extends ModMenuTypeRegistry {

    @Override
    public void init() {
        entries.forEach(this::registerEntry);
    }

    @Override
    public <M extends AbstractContainerMenu> MinecraftEntry<MenuType<M>> register(String path, BiFunction<Integer, Inventory, M> factory) {
        return register(path, () -> new MenuType<>(factory::apply, FeatureFlags.VANILLA_SET));
    }

    @Override
    public <E extends MenuType<?>> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, supplier, tags);
        entries.put(path, entry);
        return entry;
    }

    private <E extends MenuType<?>> void registerEntry(String path, MinecraftEntry<E> entry) {
        entry.set(Registry.register(BuiltInRegistries.MENU, entry.key(), entry.get()));
    }

}
