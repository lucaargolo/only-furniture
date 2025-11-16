package dev.lucaargolo.furniture;


import dev.lucaargolo.furniture.client.FurnitureModClient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.function.Supplier;

@Mod(FurnitureMod.MOD_ID)
public class NeoForgeFurnitureMod extends FurnitureMod {

    private final IEventBus modBus;

    public NeoForgeFurnitureMod(IEventBus modBus) {
        this.modBus = modBus;
        this.init();
        if(FMLEnvironment.dist.isClient()) {
            this.loadPlatformClass(FurnitureModClient.class);
        }
    }

    @Override
    public String getPlatform() {
        return "NeoForge";
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public Block getPottedBlock(Block block) {
        FlowerPotBlock potBlock = (FlowerPotBlock) Blocks.FLOWER_POT;
        Supplier<? extends Block> pottedBlockSupplier = potBlock.getFullPotsView().getOrDefault(BuiltInRegistries.BLOCK.getKey(block), () -> Blocks.FLOWER_POT);
        return pottedBlockSupplier.get();
    }

    public static IEventBus getModBus() {
        return ((NeoForgeFurnitureMod) FurnitureMod.INSTANCE).modBus;
    }

}
