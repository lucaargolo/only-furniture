package dev.lucaargolo.furniture.attachment;

import org.jetbrains.annotations.Nullable;

public abstract class ModAttachmentManager {

    @Nullable
    public abstract <A extends DataAttachment<A>> A get(Object target, DataAttachmentType<A> type);

    public abstract <A extends DataAttachment<A>> A set(Object target, DataAttachmentType<A> type, @Nullable A value);

    public <A extends DataAttachment<A>> A getOrCreate(Object target, DataAttachmentType<A> type) {
        A a = get(target, type);
        if(a == null) {
            a = type.create();
            set(target, type, a);
        }
        return a;
    }

}
