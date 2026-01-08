package dev.lucaargolo.furniture.block.behaviour;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.animation.AnimationDefinition;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.AnimationDataAttachment;
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

import java.util.Optional;
import java.util.function.Supplier;

public class StorageBehaviour extends Behaviour<StorageBehaviour> {

    private final int size;
    private final Component title;

    @Nullable
    private final Supplier<AnimationDefinition> openAnimation;
    @Nullable
    private final Supplier<AnimationDefinition> closeAnimation;

    public StorageBehaviour(Vec3 pos, int size, Component title, @Nullable Supplier<AnimationDefinition> openAnimation, @Nullable Supplier<AnimationDefinition> closeAnimation) {
        super(pos);
        this.size = size;
        this.title = title;
        this.openAnimation = openAnimation;
        this.closeAnimation = closeAnimation;
    }

    public StorageBehaviour(Vec3 pos, int size, Component title) {
        this(pos, size, title, null, null);
    }

    @Override
    public StorageBehaviour positioned(Vec3 pos) {
        return new StorageBehaviour(pos, this.size, this.title, this.openAnimation, this.closeAnimation);
    }

    @Override
    public boolean interact(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index) {
        assert blockEntity != null;

        StorageDataAttachment storageData = ModDataAttachments.STORAGE_DATA.getOrCreate(blockEntity);
        NonNullList<ItemStack> storage = storageData.getStorage(index);
        if(storage == null || storage.size() < this.size) {
            if(storage != null) {
                Containers.dropContents(level, pos, storage);
            }
            storage = NonNullList.withSize(this.size, ItemStack.EMPTY);
            ModDataAttachments.STORAGE_DATA.set(blockEntity, storageData.set(index, storage));
        }
        SimpleContainer container = new SimpleContainer(this.size);
        ((SimpleContainerAccessor) container).setItems(storage);
        container.addListener(c -> ModDataAttachments.STORAGE_DATA.set(blockEntity, storageData.set(index, container.getItems())));

        if(this.openAnimation != null) {
            AnimationDataAttachment animationData = ModDataAttachments.ANIMATION_DATA.getOrCreate(blockEntity);
            ModDataAttachments.ANIMATION_DATA.set(blockEntity, animationData.replace(level, pos, state, this.openAnimation.get().animation()));
        }

        StorageMenu.Definition definition;
        if(this.closeAnimation != null) {
            definition = new StorageMenu.Definition(pos, this.size, Optional.of(this.closeAnimation.get().animation()));
        }else{
            definition = new StorageMenu.Definition(pos, this.size, Optional.empty());
        }

        FurnitureMod.getInstance().openMenu(ModMenuTypes.STORAGE, StorageMenu::new, player, container, definition, this.title);

        return true;
    }

    @Override
    public void remove(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, int index) {
        assert blockEntity != null;

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
