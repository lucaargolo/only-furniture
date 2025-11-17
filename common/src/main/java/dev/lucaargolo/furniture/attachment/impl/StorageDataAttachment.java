package dev.lucaargolo.furniture.attachment.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.furniture.attachment.DataAttachment;
import dev.lucaargolo.furniture.attachment.DataAttachmentType;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StorageDataAttachment implements DataAttachment<StorageDataAttachment> {

    private static final Codec<Integer> INT_KEY_CODEC = Codec.STRING.xmap(Integer::valueOf, Object::toString);

    private static final Codec<NonNullList<ItemStack>> STORAGE_CODEC = ItemStack.OPTIONAL_CODEC.listOf()
            .xmap(list -> NonNullList.of(ItemStack.EMPTY, list.toArray(ItemStack[]::new)), list -> list);

    public static final Codec<StorageDataAttachment> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Codec.unboundedMap(INT_KEY_CODEC, STORAGE_CODEC).fieldOf("data").forGetter(StorageDataAttachment::get))
            .apply(instance, StorageDataAttachment::new)
    );

    private final Map<Integer, NonNullList<ItemStack>> data;

    public StorageDataAttachment(Map<Integer, NonNullList<ItemStack>> data) {
        this.data = new HashMap<>(data);
    }

    private Map<Integer, NonNullList<ItemStack>> get() {
        return this.data;
    }

    @Nullable
    public NonNullList<ItemStack> getStorage(int index) {
        return data.get(index);
    }

    public StorageDataAttachment set(int index, NonNullList<ItemStack> storage) {
        data.put(index, storage);
        return this;
    }

    @Override
    public DataAttachmentType<StorageDataAttachment> getType() {
        return ModDataAttachments.STORAGE_DATA;
    }

}
