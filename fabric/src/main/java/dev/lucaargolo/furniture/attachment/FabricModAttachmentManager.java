package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.registry.FabricModAttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FabricModAttachmentManager extends ModAttachmentManager{

    @Override
    public <A extends DataAttachment<A>> @Nullable A get(Object target, DataAttachmentType<A> type) {
        FabricModAttachmentRegistry registry = (FabricModAttachmentRegistry) ModDataAttachments.REGISTRY;
        return ((AttachmentTarget) target).getAttached(registry.get(type));
    }

    @Override
    public <A extends DataAttachment<A>> A set(Object target, DataAttachmentType<A> type, @Nullable A value) {
        FabricModAttachmentRegistry registry = (FabricModAttachmentRegistry) ModDataAttachments.REGISTRY;
        A result = ((AttachmentTarget) target).setAttached(registry.get(type), value);
        if(target instanceof BlockEntity entity) {
            Level level = entity.getLevel();
            BlockPos pos = entity.getBlockPos();
            FurnitureMod.updateBlock(level, pos);
        }
        return result;
    }


}
