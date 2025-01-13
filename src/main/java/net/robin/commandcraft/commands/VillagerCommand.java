package net.robin.commandcraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import static net.minecraft.server.command.CommandManager.literal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.robin.commandcraft.utils.VillagerUtils;

public class VillagerCommand {

    /**
     * Villager Command Registration
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("villager")
                .then(CommandManager.literal("getbed")
                        .executes(context -> getVillagerBed(context.getSource())))
                .then(CommandManager.literal("getworkstation")
                        .executes(context -> getVillagerWorkstation(context.getSource())))
                .then(CommandManager.literal("kill")
                        .executes(context -> killNearestVillager(context.getSource())))
                .then(CommandManager.literal("gotobed")
                        .executes(context -> moveVillagerToBed(context.getSource())))
                .then(CommandManager.literal("gotoworkstation")
                        .executes(context -> moveVillagerToWorkstation(context.getSource())))
                .then(CommandManager.literal("startconversation")
                        .executes(context -> startConversation(context.getSource())))
                .then(CommandManager.literal("getConversationState")
                        .executes(context -> getConversationState(context.getSource())))
                .then(CommandManager.literal("getdebug")
                        .executes(context -> printDebug(context.getSource()))));
    }

    /**
     * Command logic for fetching the villager's bed
      */
    private static int getVillagerBed(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();
            GlobalPos bedPos = VillagerUtils.getVillagerBedPosition(villager);

            if (bedPos != null) {
                BlockPos blockPos = bedPos.getPos();
                player.sendMessage(Text.literal("Villager's bed is at: " + blockPos.toShortString()), false);
            } else {
                player.sendMessage(Text.literal("This villager has no registered bed."), false);
            }
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    /**
     * Command logic for fetching the villager's workstation
      */
    private static int getVillagerWorkstation(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();
            GlobalPos workstationPos = VillagerUtils.getVillagerWorkstationPosition(villager);

            if (workstationPos != null) {
                BlockPos blockPos = workstationPos.getPos();
                player.sendMessage(Text.literal("Villager's workstation is at: " + blockPos.toShortString()), false);
            } else {
                player.sendMessage(Text.literal("This villager has no registered workstation."), false);
            }
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    /**
     * Command logic for killing the nearest villager
      */
    private static int killNearestVillager(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();
            villager.kill();
            player.sendMessage(Text.literal("Villager killed."), false);
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    /**
     * Command logic to make the nearest villager go to its bed
      */
    private static int moveVillagerToBed(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();
            VillagerUtils.moveVillagerToBed(villager);
            player.sendMessage(Text.literal("Villager is moving to their bed."), false);
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    /**
     * Command logic to make the nearest villager go to its workstation
      */
    private static int moveVillagerToWorkstation(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();
            VillagerUtils.moveVillagerToWorkstation(villager);
            player.sendMessage(Text.literal("Villager is moving to their workstation."), false);
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    /**
     * Command logic to print some debug messages related to villager
      */
    private static int printDebug(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            // 0. get villager's brain
            VillagerEntity villager = nearestVillager.get();
            Brain<VillagerEntity> villagerBrain = villager.getBrain();
            // 0.5. print a separation line
            player.sendMessage(Text.literal("----------------------------------------------------"));
            // 1. get and print running tasks
            List<Task<? super VillagerEntity>> runningTasks = villagerBrain.getRunningTasks();
            String result1 = runningTasks.toString();
            System.out.println(result1);
            player.sendMessage(Text.literal("Active Tasks: "));
            player.sendMessage(Text.literal(result1), false);
            // 2. get and print possibleActivities
            Set<Activity> possibleActivities = villagerBrain.getPossibleActivities();
            String result2 = possibleActivities.toString();
            System.out.println(result2);
            player.sendMessage(Text.literal("Active Activities: "));
            player.sendMessage(Text.literal(result2));
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    /**
     * Command logic to force a villager into conversation
      */
    private static int startConversation(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();
            VillagerUtils.startConversation(villager);
            player.sendMessage(Text.literal("Conversation began."), false);
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    /**
     * Command logic to check whether a villager is in a conversation
      */
    private static int getConversationState(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();
            boolean isInConversation = VillagerUtils.isInConversation(villager);
            if (isInConversation) {
                player.sendMessage(Text.literal("Villager is in conversation"), false);
            } else {
                player.sendMessage(Text.literal("Villager is not in conversation"), false);
            }
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    /**
     * Finds the nearest villager
     * @return The nearest villager
     */
    private static Optional<VillagerEntity> getNearestVillager(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = source.getWorld();
        return VillagerUtils.getNearestVillager(world, player, 10);
    }
}
