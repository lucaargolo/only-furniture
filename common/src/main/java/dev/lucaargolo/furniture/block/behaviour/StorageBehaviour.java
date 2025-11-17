package dev.lucaargolo.furniture.block.behaviour;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.StorageDataAttachment;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.menu.ModMenuTypes;
import dev.lucaargolo.furniture.menu.StorageMenu;
import dev.lucaargolo.furniture.mixin.SimpleContainerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
        SimpleContainer container = new SimpleContainer(this.size);
        ((SimpleContainerAccessor) container).setItems(storage);
        container.addListener(c -> ModDataAttachments.STORAGE_DATA.set(blockEntity, storageData.set(index, container.getItems())));
        FurnitureMod.getInstance().openMenu(ModMenuTypes.STORAGE, StorageMenu::new, player, container, this.size, Component.literal("Storage"));
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
