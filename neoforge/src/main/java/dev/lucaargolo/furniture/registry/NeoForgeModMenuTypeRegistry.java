package dev.lucaargolo.furniture.registry;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import dev.lucaargolo.furniture.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class NeoForgeModMenuTypeRegistry extends ModMenuTypeRegistry {

    private final DeferredRegister<MenuType<?>> registry = DeferredRegister.create(Registries.MENU, FurnitureMod.MOD_ID);

    @Override
    public void init() {
        this.registry.register(NeoForgeFurnitureMod.getModBus());
    }

    @Override
    public <M extends AbstractContainerMenu> MinecraftEntry<MenuType<M>> register(String path, BiFunction<Integer, Inventory, M> factory) {
        return register(path, () -> new MenuType<>(factory::apply, FeatureFlags.VANILLA_SET));
    }

    @Override
    public <E extends MenuType<?>> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, this.registry.register(path, supplier), tags);
        entries.put(path, entry);
        return entry;
    }

}
