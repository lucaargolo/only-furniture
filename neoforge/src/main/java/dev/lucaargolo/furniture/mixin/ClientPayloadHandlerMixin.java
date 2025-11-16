package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.SyncAttachmentsPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ClientPayloadHandler.class)
public class ClientPayloadHandlerMixin {

    @Inject(at = @At("TAIL"), method = "handle(Lnet/neoforged/neoforge/network/payload/SyncAttachmentsPayload;Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V")
    private static void handle(SyncAttachmentsPayload payload, IPayloadContext context, CallbackInfo ci) {
        if(payload.target() instanceof SyncAttachmentsPayload.BlockEntityTarget(BlockPos pos)) {
            BlockEntity entity = context.player().level().getBlockEntity(pos);
            if(entity != null && entity.getType().builtInRegistryHolder().is(key -> key.location().getNamespace().equals(FurnitureMod.MOD_ID))) {
                Minecraft.getInstance().levelRenderer.setSectionDirty(
                        SectionPos.blockToSectionCoord(pos.getX()),
                        SectionPos.blockToSectionCoord(pos.getY()),
                        SectionPos.blockToSectionCoord(pos.getZ())
                );
            }
        }
    }

}
