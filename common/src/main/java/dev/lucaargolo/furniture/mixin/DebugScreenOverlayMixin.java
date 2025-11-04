package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Shadow @Final private Minecraft minecraft;

    @SuppressWarnings("DiscouragedShift") // If the shift ends up breaking the worst that will happen is that the string order will be a bit wrong
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", shift = At.Shift.BY, by = 5), method = "getSystemInformation", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void furniture$addFurnitureDataInformation(CallbackInfoReturnable<List<String>> cir, long maxMemory, long totalMemory, long freeMemory, long usedMemory, List<String> list, BlockPos blockPos, BlockState blockState) {
        assert minecraft.level != null;
        if(blockState.getBlock() instanceof FurnitureBlock) {
            int layer = blockState.getValue(FurnitureBlock.LAYER);
            FurnitureData data = FurnitureData.get(minecraft.level, blockPos, layer);
            if(data.hasOriginal()) {
                Direction facing = Direction.fromYRot(data.getRotation() + 180);
                list.add("furniture/rotation: " + data.getRotation() + " (" + facing + ")");
                list.add("furniture/x: " + data.getX());
                list.add("furniture/z: " + data.getZ());
            }
        }

    }

}
