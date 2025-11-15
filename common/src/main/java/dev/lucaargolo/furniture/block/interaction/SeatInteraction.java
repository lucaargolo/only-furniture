package dev.lucaargolo.furniture.block.interaction;

import dev.lucaargolo.furniture.entity.SeatEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record SeatInteraction(Vec3 pos) implements Interaction<SeatInteraction> {

    @Override
    public SeatInteraction positioned(Vec3 pos) {
        return new SeatInteraction(pos);
    }

    @Override
    public boolean interact(Level level, Player player, BlockHitResult hitResult) {
        List<SeatEntity> seatEntities = level.getEntitiesOfClass(SeatEntity.class, AABB.ofSize(pos, 0.1, 0.1, 0.1));
        boolean isSeatFree = seatEntities.isEmpty();
        if(!isSeatFree) {
            isSeatFree = ejectSeatedExceptPlayer(level, seatEntities.getFirst());
        }
        if(!isSeatFree || player.isPassenger()) {
            return false;
        }

        if (!level.isClientSide) {
            SeatEntity seat = new SeatEntity(level, pos, hitResult.getBlockPos());
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
