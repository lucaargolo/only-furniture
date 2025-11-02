/*
    MIT License

    Copyright (c) 2022 Starfish Studios, Inc.

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

    Code extracted from:
        https://github.com/starfish-studios/AnotherFurniture/blob/1.21.1/common/src/main/java/com/starfish_studios/another_furniture/block/SeatBlock.java
    At:
        02/11/2024
 */

package dev.lucaargolo.furniture.block;

import com.mojang.math.Axis;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.entity.SeatEntity;
import dev.lucaargolo.furniture.utils.FurnitureData;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FurnitureSeatBlock extends FurnitureBlock {

    private final Vec3[] seats;

    public FurnitureSeatBlock(Block base, VoxelShape[] shapes, Vec3... seats) {
        super(base, shapes);
        this.seats = seats;
    }

    public Vec3[] getSeats() {
        return seats;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if(tryAndSit(level, pos, player, hitResult)) {
            return InteractionResult.SUCCESS;
        }else{
            return InteractionResult.PASS;
        }
    }

    public boolean tryAndSit(@NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (FurnitureMod.INSTANCE.isFakePlayer(player)) return false;
        if (!level.mayInteract(player, pos)) return false;
        if (player.isPassenger() || player.isCrouching()) return false;
        FurnitureData[] layers = FurnitureData.get(level, pos);
        for(FurnitureData data : layers) {
            if (data.hasOriginal()) {
                Int2ObjectMap<SeatEntity> activeSeats = this.getActiveSeats(level, pos);
                Int2ObjectMap<Vec3> freeSeats = new Int2ObjectArrayMap<>();
                if (freeSeats.isEmpty()) {
                    for (int i = 0; i < this.seats.length; i++) {
                        if (!activeSeats.containsKey(i)) {
                            freeSeats.put(i, getPositionForSeat(data, pos, i));
                        }
                    }
                }

                if (freeSeats.isEmpty()) {
                    for (int i = 0; i < this.seats.length; i++) {
                        if (activeSeats.containsKey(i) && ejectSeatedExceptPlayer(level, activeSeats.get(i))) {
                            freeSeats.put(i, getPositionForSeat(data, pos, i));
                            break;
                        }
                    }
                }

                if (freeSeats.isEmpty()) {
                    return false;
                }

                if (!level.isClientSide) {
                    int seat = freeSeats.int2ObjectEntrySet().stream()
                            .min(Comparator.comparingDouble(e -> e.getValue().distanceToSqr(hitResult.getLocation())))
                            .map(Int2ObjectMap.Entry::getIntKey)
                            .orElseThrow();
                    sitDown(level, pos, getLeashed(player).orElse(player), seat);
                }
                return true;
            }
        }
        return false;
    }

    private void sitDown(Level level, BlockPos pos, Entity entity, int index) {
        FurnitureData[] layers = FurnitureData.get(level, pos);
        for(FurnitureData data : layers) {
            if(data.hasOriginal()) {
                SeatEntity seat = new SeatEntity(level, this.getPositionForSeat(data, pos, index), pos);
                level.addFreshEntity(seat);
                entity.startRiding(seat);

                level.updateNeighbourForOutputSignal(pos, level.getBlockState(pos).getBlock());

                if (entity instanceof TamableAnimal ta) ta.setInSittingPose(true);
                break;
            }
        }
    }

    public Vec3 getPositionForSeat(FurnitureData data, BlockPos pos, int seatIndex) {
        Vec3 position = Vec3.atBottomCenterOf(pos).add(data.getX(), 0.0, data.getZ());
        Vec3 seatPosition = this.seats[seatIndex];

        Quaternionf rotation = Axis.YP.rotationDegrees(data.getRotation());
        Matrix4f transform = new Matrix4f().rotate(rotation);
        Vector4f seatOffset = new Vector4f((float) seatPosition.x, (float) seatPosition.y, (float) seatPosition.z, 1f);
        seatOffset.mul(transform);

        return position.add(seatOffset.x, seatOffset.y, seatOffset.z);
    }

    private Int2ObjectMap<SeatEntity> getActiveSeats(Level level, BlockPos pos) {
        Int2ObjectMap<SeatEntity> seats = new Int2ObjectArrayMap<>();
        FurnitureData[] layers = FurnitureData.get(level, pos);
        for(FurnitureData data : layers) {
            if (data.hasOriginal()) {
                for(int i = 0; i < this.seats.length; i++) {
                    Vec3 position = this.getPositionForSeat(data, pos, i);
                    AABB bounds = AABB.ofSize(position, 0.1, 0.1, 0.1);
                    List<SeatEntity> entities = level.getEntitiesOfClass(SeatEntity.class, bounds);
                    if(!entities.isEmpty()) {
                        seats.put(i, entities.getFirst());
                    }
                }
                break;
            }
        }
        return seats;
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