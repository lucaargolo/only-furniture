package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Unique
    private BlockPos furniture$originalBlockPos;

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "handleUseItemOn", ordinal = 0)
    private BlockPos modifyBlockPos(BlockPos original) {
        this.furniture$originalBlockPos = original;
        return original.mutable();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canInteractWithBlock(Lnet/minecraft/core/BlockPos;D)Z"), method = "handleUseItemOn", locals = LocalCapture.CAPTURE_FAILSOFT)
    private void furniture$changeBlockPosToAllowFurnitureLocationOffset(ServerboundUseItemOnPacket packet, CallbackInfo ci, ServerLevel serverlevel, InteractionHand interactionhand, ItemStack itemstack, BlockHitResult blockhitresult, Vec3 vec3, BlockPos blockpos) {
        BlockState state = serverlevel.getBlockState(blockpos);
        if(state.getBlock() instanceof FurnitureBlock) {
            BlockPos.MutableBlockPos mutable = (BlockPos.MutableBlockPos) blockpos;
            mutable.set(vec3.x, vec3.y, vec3.z);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getDirection()Lnet/minecraft/core/Direction;"), method = "handleUseItemOn", locals = LocalCapture.CAPTURE_FAILSOFT)
    private void furniture$restoreBlockPosAfterAllowingFurnitureLocationOffset(ServerboundUseItemOnPacket packet, CallbackInfo ci, ServerLevel serverlevel, InteractionHand interactionhand, ItemStack itemstack, BlockHitResult blockhitresult, Vec3 vec3, BlockPos blockpos) {
        BlockPos.MutableBlockPos mutable = (BlockPos.MutableBlockPos) blockpos;
        mutable.set(this.furniture$originalBlockPos);
    }

}
