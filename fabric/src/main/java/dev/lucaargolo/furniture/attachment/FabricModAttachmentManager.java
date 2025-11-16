package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.registry.FabricModAttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FabricModAttachmentManager extends ModAttachmentManager{

    @Override
    public <A extends DataAttachment<A>> @Nullable A get(Object target, DataAttachmentType<A> type) {
        FabricModAttachmentRegistry registry = (FabricModAttachmentRegistry) ModDataAttachments.REGISTRY;
        return ((AttachmentTarget) target).getAttached(registry.get(type));
    }

    @Override
    public <A extends DataAttachment<A>> A set(Object target, DataAttachmentType<A> type, A value) {
        FabricModAttachmentRegistry registry = (FabricModAttachmentRegistry) ModDataAttachments.REGISTRY;
        return ((AttachmentTarget) target).setAttached(registry.get(type), value);
    }


}
