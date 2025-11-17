package dev.lucaargolo.furniture.block.behaviour;

import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.StorageDataAttachment;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.menu.StorageMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageBehaviour extends Behaviour<StorageBehaviour> {

    private final int size;

    public StorageBehaviour(Vec3 pos, int size) {
        super(pos);
        this.size = size;
    }

    @Override
    public StorageBehaviour positioned(Vec3 pos) {
        return new StorageBehaviour(pos, this.size);
    }

    @Override
    public boolean interact(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index) {
        StorageDataAttachment storageData = ModDataAttachments.STORAGE_DATA.getOrCreate(blockEntity);
        NonNullList<ItemStack> storage = storageData.getStorage(index);
        if(storage == null) {
            storage = NonNullList.withSize(this.size, ItemStack.EMPTY);
            ModDataAttachments.STORAGE_DATA.set(blockEntity, storageData.set(index, storage));
        }
        player.openMenu(new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.empty();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return new StorageMenu(containerId, playerInventory);
            }
        });
        return true;
    }

    @Override
    public void remove(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, int index) {
        StorageDataAttachment storageData = ModDataAttachments.STORAGE_DATA.get(blockEntity);
        if(storageData != null) {
            NonNullList<ItemStack> storage = storageData.getStorage(index);
            if(storage != null) {
                Containers.dropContents(level, pos, storage);
            }
        }
    }

    @Override
    public boolean isBlockEntityNeeded() {
        return true;
    }

}
