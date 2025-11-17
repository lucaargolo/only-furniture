package dev.lucaargolo.furniture.utils;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WrappedContainer implements Container {

    private Container wrapped;

    public WrappedContainer(Container wrapped) {
        this.wrapped = wrapped;
    }

    public WrappedContainer(int size) {
        this.wrapped = new SimpleContainer(size);
    }

    public Container getWrapped() {
        return wrapped;
    }

    public void setWrapped(Player player, Container wrapped) {
        this.wrapped.stopOpen(player);
        this.wrapped = wrapped;
        this.wrapped.startOpen(player);
    }

    @Override
    public int getContainerSize() {
        return wrapped.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return wrapped.getItem(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return wrapped.removeItem(slot, amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return wrapped.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        wrapped.setItem(slot, stack);
    }

    @Override
    public void setChanged() {
        wrapped.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return wrapped.stillValid(player);
    }

    @Override
    public void clearContent() {
        wrapped.clearContent();
    }


}
