package dev.lucaargolo.furniture.menu;

import dev.lucaargolo.furniture.utils.WrappedContainer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StorageMenu extends AbstractContainerMenu {

    private final int rows;
    private final int cols;

    private final WrappedContainer container;
    private final Player player;

    public StorageMenu(int containerId, Inventory playerInventory, int slots) {
        super(ModMenuTypes.STORAGE.get(), containerId);
        this.container = new WrappedContainer(slots);
        this.player = playerInventory.player;

        int cols = Math.min(slots, 9);
        for (int c = cols; c >= 1; c--) {
            if (slots % c == 0 || c == 1) {
                cols = c;
                break;
            }
        }

        int rows = Mth.ceil((float) slots / cols);
        if(rows > 9) {
            cols = Math.min(slots, 9);
            rows = Mth.ceil((float) slots / cols);
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

    public void wrap(Container container) {
        this.container.setWrapped(this.player, container);
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
        return true;
    }


}
