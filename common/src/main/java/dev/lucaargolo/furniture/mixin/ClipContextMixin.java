package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClipContext.class)
public class ClipContextMixin {

    @Shadow @Final private CollisionContext collisionContext;

    @Inject(at = @At("HEAD"), method = "getBlockShape", cancellable = true)
    public void furniture$cancelIfReplaceable(BlockState pBlockState, BlockGetter pLevel, BlockPos pPos, CallbackInfoReturnable<VoxelShape> cir) {
        if(pBlockState.canBeReplaced() && this.collisionContext instanceof EntityCollisionContextAccessor accessor && accessor.getHeldItem().getItem() instanceof FurnitureBlockItem) {
            cir.setReturnValue(Shapes.empty());
        }
    }
}
