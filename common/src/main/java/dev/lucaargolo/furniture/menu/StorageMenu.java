package dev.lucaargolo.furniture.menu;

import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StorageMenu extends AbstractContainerMenu {

    private final int rows;
    private final int cols;

    private final Container container;

    public StorageMenu(int containerId, Inventory playerInventory, int slots) {
        this(containerId, playerInventory, new SimpleContainer(slots));
    }

    public StorageMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.STORAGE.get(), containerId);
        this.container = container;
        this.container.startOpen(playerInventory.player);

        int cols = Math.min(container.getContainerSize(), 9);
        for (int c = cols; c >= 1; c--) {
            if (container.getContainerSize() % c == 0 || c == 1) {
                cols = c;
                break;
            }
        }

        int rows = Mth.ceil((float) container.getContainerSize() / cols);
        if(rows > 9) {
            cols = Math.min(container.getContainerSize(), 9);
            rows = Mth.ceil((float) container.getContainerSize() / cols);
        }

        this.rows = rows;
        this.cols = cols;

        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.cols; col++) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        int offset = 43 + this.rows * 18;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, row * 18 + offset));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 58 + offset));
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();
            if (index < this.rows * this.cols) {
                if (!this.moveItemStackTo(slotStack, this.rows * this.cols, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, this.rows * this.cols, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }


}
