package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.FurnitureMod;

public interface DataAttachment<A extends DataAttachment<A>> {

    @SuppressWarnings("unchecked")
    default DataAttachmentType<A> getType() {
        return FurnitureMod.INSTANCE.getAttachmentManager().getType(this.getClass());
    }

}
