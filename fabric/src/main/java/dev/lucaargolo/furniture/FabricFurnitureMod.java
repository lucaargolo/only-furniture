package dev.lucaargolo.furniture;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FabricFurnitureMod extends FurnitureMod implements ModInitializer {

    @Override
    public void onInitialize() {
        this.init();
        PlayerBlockBreakEvents.BEFORE.register(this::beforeBlockBreak);
    }

    private boolean beforeBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        return !(world instanceof ServerLevel level) || !this.beforeBlockBreak(state, level, pos, player);
    }

    @Override
    public String getPlatform() {
        return "Fabric";
    }

}
