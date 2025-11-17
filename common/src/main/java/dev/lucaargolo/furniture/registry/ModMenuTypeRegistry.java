package dev.lucaargolo.furniture.registry;

import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class ModMenuTypeRegistry extends MinecraftRegistry<MenuType<?>, MinecraftEntry<? extends MenuType<?>>> {

    public ModMenuTypeRegistry() {
        super(Registries.MENU);
    }

    public abstract <M extends AbstractContainerMenu> MinecraftEntry<MenuType<M>> register(String path, BiFunction<Integer, Inventory, M> factory);

    @Override
    public abstract <E extends MenuType<?>> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags);

    @Override
    protected <E extends MenuType<?>> MinecraftEntry<E> entry(String path, Supplier<E> supplier, TagKey<?>... tags) {
        return new MinecraftEntry<>(id++, path, supplier, tags);
    }

}