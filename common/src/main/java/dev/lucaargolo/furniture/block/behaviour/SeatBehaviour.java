package dev.lucaargolo.furniture.block.behaviour;

import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class SeatBehaviour extends Behaviour<SeatBehaviour> {

    public SeatBehaviour(Vec3 pos) {
        super(pos);
    }

    @Override
    public SeatBehaviour positioned(Vec3 pos) {
        return new SeatBehaviour(pos);
    }

    @Override
    public boolean interact(Level level, BlockPos pos, BlockState state, @Nullable FurnitureBlockEntity blockEntity, Player player, int index) {
        List<SeatEntity> seatEntities = level.getEntitiesOfClass(SeatEntity.class, AABB.ofSize(this.pos, 0.1, 0.1, 0.1));
        boolean isSeatFree = seatEntities.isEmpty();
        if(!isSeatFree) {
            isSeatFree = ejectSeatedExceptPlayer(level, seatEntities.getFirst());
        }
        if(!isSeatFree || player.isPassenger()) {
            return false;
        }

        if (!level.isClientSide) {
            SeatEntity seat = new SeatEntity(level, this.pos, pos);
            level.addFreshEntity(seat);

            Entity entity = getLeashed(player).orElse(player);
            entity.startRiding(seat);

            if (entity instanceof TamableAnimal ta)
                ta.setInSittingPose(true);
        }
        return true;
    }

    private static Optional<Entity> getLeashed(Player player) {
        List<Entity> entities = player.level().getEntities((Entity) null, player.getBoundingBox().inflate(10), e -> true);
        for (Entity e : entities)
            if (e instanceof Mob mob && mob.getLeashHolder() == player && canBePickedUp(e)) return Optional.of(mob);
        return Optional.empty();
    }

    private static boolean ejectSeatedExceptPlayer(Level level, SeatEntity seatEntity) {
        List<Entity> passengers = seatEntity.getPassengers();
        if (!passengers.isEmpty() && passengers.getFirst() instanceof Player) return false;
        if (!level.isClientSide) seatEntity.ejectPassengers();
        return true;
    }

    private static boolean canBePickedUp(Entity passenger) {
        if (passenger instanceof Player) return false;
        if (passenger instanceof TamableAnimal ta && !ta.isTame()) return false;
        return passenger instanceof LivingEntity;
    }

}
