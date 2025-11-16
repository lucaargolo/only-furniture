package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.registry.NeoForgeModAttachmentRegistry;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public class NeoForgeModAttachmentManager extends ModAttachmentManager{

    @Override
    public <A extends DataAttachment<A>> @Nullable A get(Object target, DataAttachmentType<A> type) {
        NeoForgeModAttachmentRegistry registry = (NeoForgeModAttachmentRegistry) ModDataAttachments.REGISTRY;
        return ((IAttachmentHolder) target).getData(registry.get(type));
    }

    @Override
    public <A extends DataAttachment<A>> A set(Object target, DataAttachmentType<A> type, A value) {
        NeoForgeModAttachmentRegistry registry = (NeoForgeModAttachmentRegistry) ModDataAttachments.REGISTRY;
        if(value != null) {
            return ((IAttachmentHolder) target).setData(registry.get(type), value);
        }else{
            return ((IAttachmentHolder) target).removeData(registry.get(type));
        }
    }

}
