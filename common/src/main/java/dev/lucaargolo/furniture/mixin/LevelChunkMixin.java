package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {

    @Unique
    Block furniture$modifiedCheckedBlock;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z", shift = At.Shift.AFTER), method = "setBlockState", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void furniture$captureCheckedBlock(BlockPos pos, BlockState newState, boolean isMoving, CallbackInfoReturnable<BlockState> cir, int i, LevelChunkSection levelchunksection, boolean flag, int j, int k, int l, BlockState oldState, Block checkedBlock) {
        furniture$modifiedCheckedBlock = checkedBlock;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onRemove(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V", shift = At.Shift.AFTER), method = "setBlockState", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void furniture$destroyFurnitureEvenIfItWasNotEntirelyRemoved(BlockPos pos, BlockState newState, boolean isMoving, CallbackInfoReturnable<BlockState> cir, int i, LevelChunkSection levelchunksection, boolean flag, int j, int k, int l, BlockState oldState, Block checkedBlock) {
        if(oldState.getBlock() instanceof FurnitureBlock && oldState.getFluidState().createLegacyBlock().is(newState.getBlock())) {
            //We are removing the Furniture, but it might remain, so we need to replace block.
            furniture$modifiedCheckedBlock = levelchunksection.getBlockState(j, k, l).getBlock();
        }
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;"), method = "setBlockState", ordinal = 0)
    public Block furniture$modifyCheckedBlock(Block value) {
        return furniture$modifiedCheckedBlock;
    }

}
