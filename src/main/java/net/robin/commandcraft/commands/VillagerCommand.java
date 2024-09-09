package net.robin.commandcraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Optional;

import net.robin.commandcraft.utils.VillagerUtils;

public class VillagerCommand {

    // Register the commands
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
                        .executes(context -> moveVillagerToWorkstation(context.getSource()))));
    }

    // Command logic for fetching the villager's bed
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

    // Command logic for fetching the villager's workstation
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

    // Command logic for killing the nearest villager
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

    // Command logic to make the nearest villager go to its bed
    private static int moveVillagerToBed(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();
            VillagerUtils.moveVillagerToBed(villager);
            player.sendMessage(Text.literal("Villager is moving to its bed."), false);
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    // Command logic to make the nearest villager go to its workstation
    private static int moveVillagerToWorkstation(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Optional<VillagerEntity> nearestVillager = getNearestVillager(source);
        if (nearestVillager.isPresent()) {
            VillagerEntity villager = nearestVillager.get();
            VillagerUtils.moveVillagerToWorkstation(villager);
            player.sendMessage(Text.literal("Villager is moving to its workstation."), false);
        } else {
            player.sendMessage(Text.literal("No villager nearby."), false);
        }
        return 1;
    }

    // Find the nearest villager
    private static Optional<VillagerEntity> getNearestVillager(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = source.getWorld();
        return VillagerUtils.getNearestVillager(world, player, 10);
    }
}
