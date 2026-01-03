package dev.lucaargolo.furniture.menu;

import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.AnimationDataAttachment;
import dev.lucaargolo.furniture.utils.Animation;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class StorageMenu extends AbstractContainerMenu {

    private final int rows;
    private final int cols;

    private final Container container;
    private final Definition definition;

    public StorageMenu(int containerId, Inventory playerInventory, Definition definition) {
        this(containerId, playerInventory, new SimpleContainer(definition.size), definition);
    }

    public StorageMenu(int containerId, Inventory playerInventory, Container container, Definition definition) {
        super(ModMenuTypes.STORAGE.get(), containerId);
        this.container = container;
        this.definition = definition;

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
                this.addSlot(new Slot(container, col + row * cols, 8 + col * 18, 18 + row * 18));
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

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        BlockPos pos = this.definition.pos();
        Optional<Animation> animation = this.definition.closeAnimation();
        if(animation.isPresent()) {
            BlockEntity entity = player.level().getBlockEntity(pos);
            if(entity != null) {
                AnimationDataAttachment animations = ModDataAttachments.ANIMATION_DATA.getOrCreate(entity);
                ModDataAttachments.ANIMATION_DATA.set(entity, animations.replace(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), animation.get()));
            }
        }

    }

    public record Definition(BlockPos pos, int size, Optional<Animation> closeAnimation) {

        public static StreamCodec<ByteBuf, Definition> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                Definition::pos,
                ByteBufCodecs.VAR_INT,
                Definition::size,
                ByteBufCodecs.optional(Animation.STREAM_CODEC),
                Definition::closeAnimation,
                Definition::new
        );

    }

}
