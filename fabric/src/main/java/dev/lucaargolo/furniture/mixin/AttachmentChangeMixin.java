package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.FurnitureMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SuppressWarnings("UnstableApiUsage")
@Mixin(AttachmentChange.class)
public class AttachmentChangeMixin {

    @Inject(at = @At("TAIL"), method = "tryApply", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void tryApply(Level world, CallbackInfo ci, AttachmentTarget target) {
        if(world.isClientSide() && target instanceof BlockEntity entity && entity.getType().builtInRegistryHolder().is(key -> key.location().getNamespace().equals(FurnitureMod.MOD_ID))) {
            BlockPos pos = entity.getBlockPos();
            Minecraft.getInstance().levelRenderer.setSectionDirty(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getY()),
                SectionPos.blockToSectionCoord(pos.getZ())
            );
        }
    }

}
