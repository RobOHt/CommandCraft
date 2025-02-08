package net.robin.commandcraft.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.Vec3d;
import net.robin.commandcraft.villagerstate.VillagerState;
import net.robin.commandcraft.villagerstate.VillagerStateManager;
import java.util.Optional;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.entity.Entity;
import net.robin.commandcraft.villagertask.ConversationTask;


public class VillagerUtils {

    /**
     * Gets the position of the villager's bed (home)
     */
    public static GlobalPos getVillagerBedPosition(VillagerEntity villager) {
        return villager.getBrain().getOptionalMemory(MemoryModuleType.HOME).orElse(null);
    }

    /**
     * Gets the position of the villager's workstation (job site)
      */
    public static GlobalPos getVillagerWorkstationPosition(VillagerEntity villager) {
        return villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE).orElse(null);
    }

    /**
     * Makes a villager forget its bed (home)
      */
    public static void forgetVillagerBed(VillagerEntity villager) {
        villager.getBrain().forget(MemoryModuleType.HOME);
    }

    /**
     * Makes a villager forget its workstation
      */
    public static void forgetVillagerWorkstation(VillagerEntity villager) {
        villager.getBrain().forget(MemoryModuleType.JOB_SITE);
    }


    /**
     * Gets the villager that the player is pointing at within a specified radius.
     */
    public static Optional<VillagerEntity> getNearestVillager(int radius) {
        // Find the targeted entity
        HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;

        // Check if it's a villager that is within range
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            Entity entity = entityHit.getEntity();
            if (entity instanceof VillagerEntity villager && entity.squaredDistanceTo(MinecraftClient.getInstance().player) < radius) {
                return Optional.of(villager);
            }
        }

        // No villager was found otherwise
        return Optional.empty();
    }

    /**
     * Makes a villager go to its bed
      */
    public static void moveVillagerToBed(VillagerEntity villager) {
        GlobalPos bedPos = getVillagerBedPosition(villager);
        if (bedPos != null && villager.getWorld().getRegistryKey() == bedPos.getDimension()) {
            BlockPos blockPos = bedPos.getPos();
            moveVillagerTo(villager, blockPos);
        }
    }

    /**
     * Makes a villager go to its workstation
      */
    public static void moveVillagerToWorkstation(VillagerEntity villager) {
        GlobalPos workstationPos = getVillagerWorkstationPosition(villager);
        if (workstationPos != null && villager.getWorld().getRegistryKey() == workstationPos.getDimension()) {
            BlockPos blockPos = workstationPos.getPos();
            moveVillagerTo(villager, blockPos);
        }
    }

    /**
     * Sets Villager to "in conversation mode". Triggers ConversationTask <b><u>if its trigger conditions are met</u></b>.
     * @return <code>true</code> if the conversation was started, <code>false</code> otherwise.
      */
    public static boolean startConversation(VillagerEntity villager, PlayerEntity player) {
        if (ConversationTask.taskIsAvailable(villager, player)) {
            VillagerStateManager.setState(villager, VillagerState.IN_CONVERSATION, true);
            VillagerStateManager.setState(villager, VillagerState.CONVERSATION_PARTNER, player.getUuidAsString());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets Villager to "not in conversation mode". Ends ConversationTask.
      */
    public static void endConversation(VillagerEntity villager) {
        VillagerStateManager.setState(villager, VillagerState.IN_CONVERSATION, false);
        VillagerStateManager.setState(villager, VillagerState.CONVERSATION_PARTNER, "");
    }

    /**
     * Returns whether the villager is in conversation. This is actually a signal flag for ConversationTask.
      */
    public static boolean isInConversation(VillagerEntity villager) {
        return Boolean.TRUE.equals(VillagerStateManager.getState(villager, VillagerState.IN_CONVERSATION, Boolean.class));
    }

    /**
     * Helper method to move the villager to a specific position
      */
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
