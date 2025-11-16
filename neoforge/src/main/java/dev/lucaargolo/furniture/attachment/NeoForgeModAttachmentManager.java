package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.NeoForgeModAttachmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        A result;
        if(value != null) {
            result = ((IAttachmentHolder) target).setData(registry.get(type), value);
        }else{
            result = ((IAttachmentHolder) target).removeData(registry.get(type));
        }
        if(target instanceof BlockEntity entity) {
            Level level = entity.getLevel();
            BlockPos pos = entity.getBlockPos();
            FurnitureMod.updateBlock(level, pos);
        }
        return result;
    }

}
