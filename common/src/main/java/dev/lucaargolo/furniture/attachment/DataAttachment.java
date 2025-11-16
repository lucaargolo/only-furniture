package dev.lucaargolo.furniture.attachment;

public interface DataAttachment<A extends DataAttachment<A>> {

    DataAttachmentType<A> getType();

}
