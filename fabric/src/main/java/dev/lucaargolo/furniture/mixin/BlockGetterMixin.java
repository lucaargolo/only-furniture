package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.utils.shape.FurnitureShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin {

    @Shadow BlockState getBlockState(BlockPos pos);

    @Inject(at = @At("RETURN"), method = "method_17743", cancellable = true)
    default void furniture$overrideFurnitureClip(ClipContext context, BlockPos pos, CallbackInfoReturnable<BlockHitResult> cir) {
        BlockHitResult hitResult = cir.getReturnValue();
        if(hitResult != null) {
            BlockPos hitPos = hitResult.getBlockPos();
            BlockState hitState = this.getBlockState(hitPos);
            if(hitState.getBlock() instanceof FurnitureBlock block) {
                FurnitureShape shape = block.getOriginalShape(((BlockGetter) this), pos, context);
                if(shape != null) {
                    cir.setReturnValue(new BlockHitResult(hitResult.getLocation(), hitResult.getDirection(), shape.pos(), hitResult.isInside()));
                }
            }
        }
    }

}
