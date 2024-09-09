package net.robin.commandcraft.utils;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class VillagerUtils {

    // Get the position of the villager's bed (home)
    public static GlobalPos getVillagerBedPosition(VillagerEntity villager) {
        return villager.getBrain().getOptionalMemory(MemoryModuleType.HOME).orElse(null);
    }

    // Get the position of the villager's workstation (job site)
    public static GlobalPos getVillagerWorkstationPosition(VillagerEntity villager) {
        return villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE).orElse(null);
    }

    // Make a villager forget its bed (home)
    public static void forgetVillagerBed(VillagerEntity villager) {
        villager.getBrain().forget(MemoryModuleType.HOME);
    }

    // Make a villager forget its workstation
    public static void forgetVillagerWorkstation(VillagerEntity villager) {
        villager.getBrain().forget(MemoryModuleType.JOB_SITE);
    }

    // Get the nearest villager to player within a 10-block radius
    public static Optional<VillagerEntity> getNearestVillager(ServerWorld world, ServerPlayerEntity player, int radius) {
        return world.getEntitiesByClass(VillagerEntity.class, player.getBoundingBox().expand(radius), villager -> true)
                .stream().findFirst();
    }

    // Make a villager go to its bed
    public static void moveVillagerToBed(VillagerEntity villager) {
        GlobalPos bedPos = getVillagerBedPosition(villager);
        if (bedPos != null && villager.getWorld().getRegistryKey() == bedPos.getDimension()) {
            BlockPos blockPos = bedPos.getPos();
            moveVillagerTo(villager, blockPos);
        }
    }

    // Make a villager go to its workstation
    public static void moveVillagerToWorkstation(VillagerEntity villager) {
        GlobalPos workstationPos = getVillagerWorkstationPosition(villager);
        if (workstationPos != null && villager.getWorld().getRegistryKey() == workstationPos.getDimension()) {
            BlockPos blockPos = workstationPos.getPos();
            moveVillagerTo(villager, blockPos);
        }
    }

    // Helper method to move the villager to a specific position
    private static void moveVillagerTo(VillagerEntity villager, BlockPos targetPos) {
        final double walk = 0.6;
        final double run = 1.0;
        Vec3d targetVec = new Vec3d(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
        Path path = villager.getNavigation().findPathTo(targetPos, 0);
        if (path != null) {
            villager.getNavigation().startMovingAlong(path, walk);
        } else {
            villager.getNavigation().startMovingTo(targetVec.x, targetVec.y, targetVec.z, walk);
        }
    }
}
