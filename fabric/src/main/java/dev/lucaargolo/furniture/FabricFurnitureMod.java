package dev.lucaargolo.furniture;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.world.entity.player.Player;

public class FabricFurnitureMod extends FurnitureMod implements ModInitializer {

    @Override
    public void onInitialize() {
        this.init();
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public String getPlatform() {
        return "Fabric";
    }

}
