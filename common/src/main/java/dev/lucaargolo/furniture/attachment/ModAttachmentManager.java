package dev.lucaargolo.furniture.attachment;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

public abstract class ModAttachmentManager {

    public void init() {
        register("block", BlockAttachment.class);
        register("chunk_furniture_data", ChunkFurnitureDataAttachment.class);
    }

    public abstract <A extends DataAttachment<A>> void register(String path, Class<A> attachment);

    @Nullable
    public abstract <A extends DataAttachment<A>> A get(ChunkAccess chunk, Class<A> attachment);

    public <A extends DataAttachment<A>> A getOrCreate(ChunkAccess chunk, Class<A> attachment) {
        A a = get(chunk, attachment);
        if(a == null) {
            a = DataAttachment.instantiate(attachment);
            set(chunk, a);
        }
        return a;
    }

    public abstract <A extends DataAttachment<A>> A set(ChunkAccess chunk, A value);

    @Nullable
    public abstract <A extends DataAttachment<A>> A get(BlockEntity entity, Class<A> attachment);

    public <A extends DataAttachment<A>> A getOrCreate(BlockEntity entity, Class<A> attachment) {
        A a = get(entity, attachment);
        if(a == null) {
            a = DataAttachment.instantiate(attachment);
            set(entity, a);
        }
        return a;
    }

    public abstract <A extends DataAttachment<A>> A set(BlockEntity entity, A value);


}
