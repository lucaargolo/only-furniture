package dev.lucaargolo.furniture.registry;

import dev.lucaargolo.furniture.attachment.DataAttachment;
import dev.lucaargolo.furniture.attachment.DataAttachmentType;

public abstract class ModAttachmentRegistry<T> {

    public void init() {

    }

    public abstract <A extends DataAttachment<A>> DataAttachmentType<A> register(String path, DataAttachmentType<A> type);

    public abstract <A extends DataAttachment<A>> T get(DataAttachmentType<A> type);

}
