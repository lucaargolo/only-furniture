package dev.lucaargolo.furniture.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StorageMenu extends AbstractContainerMenu {

    public StorageMenu(int containerId, Inventory playerInventory, int size) {
        this(containerId, playerInventory, new SimpleContainer(size));
    }

    public StorageMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.STORAGE.get(), containerId);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

}
