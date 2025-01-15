package net.robin.commandcraft.villagertask;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.World;
import net.robin.commandcraft.villagerstate.VillagerState;

import java.util.UUID;

import static net.robin.commandcraft.utils.VillagerUtils.endConversation;
import static net.robin.commandcraft.utils.VillagerUtils.isInConversation;
import static net.robin.commandcraft.villagerstate.VillagerStateManager.getState;


public class ConversationTask extends MultiTickTask<VillagerEntity> {
    private final float speed = 0.4F;  // Walking speed during conversation. This is purposefully slower than usual.
    private PlayerEntity conversationTarget;  // The player to talk to.
    private World world;  // The world the villager lives in.

    /**
     * This is a task for AI-based conversation with villagers. Upon entering conversation, a villager will look at you
     * in face while slowly following you around.
     * The constructor setup means no memory module is required to start this
     * task,
     */
    public ConversationTask() {
        super(ImmutableMap.of());
    }

    /**
     * ShouldRun defines criteria for a villager to run the current task, i.e. perform conversation. They are:
     *      1. the villager is alive;
     *      2. there must be a nearest visible player;
     *      3. the villager is in conversation (or rather, is asked to talk with the player).
     *
     * @param serverWorld the current server world
     * @param villagerEntity the current villager
     * @return whether the current task should run
     */
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        // Get the UUID of the player that villagerEntity is talking to
        String playerEntityUUID = getState(villagerEntity, VillagerState.CONVERSATION_PARTNER, String.class);

        // Get that playerEntity by searching for them in the world the villager is currently living in
        if (playerEntityUUID == null || playerEntityUUID == "") return false;
        this.world = villagerEntity.getWorld();
        try {
            this.conversationTarget = this.world.getPlayerByUuid(UUID.fromString(playerEntityUUID));
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());  // If somehow the player UUID is invalid, we handle it more gracefully.
        }

        // Check for start conditions
        boolean shouldStartConversation = villagerEntity.isAlive()
                                            && this.conversationTarget != null
                                            && isInConversation(villagerEntity);
        if (shouldStartConversation) {
            System.out.println("Conversation began.");
        }
        return shouldStartConversation;
    }

    /**
     * Task continuation criteria. They are {@linkplain #shouldRun shouldRun} criteria plus a number of additional
     * criteria. Those conditions are checked every tick, and if one of them fail, the conversation ends. They are there
     * to prevent villagers from keep talking to players despite being hit, or when it's time to sleep, and etc.
     */
    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        // shouldKeepConversation holds base continuation criteria.
        boolean shouldKeepConversation = villagerEntity.isAlive()
                                            && this.conversationTarget != null
                                            && isInConversation(villagerEntity)
                                            && !villagerEntity.isSleeping();
        if (!shouldKeepConversation) {
            System.out.println("Conversation ended naturally.");
            return false;
        }

        // Additional criteria 1: conversation ends when player goes too far away.
        if (villagerEntity.squaredDistanceTo(this.conversationTarget) > 25.0) {
            System.out.println("Conversation ended because the player is too far away.");
            return false;
        }

        // Additional criteria 2: conversation ends when the villager is panicking, the bell is ringing, or if there is a raid.
        Brain<?> brain = villagerEntity.getBrain();
        for (Task<?> task : brain.getRunningTasks()) {
            if (task instanceof PanicTask || task instanceof HideWhenBellRingsTask || task instanceof StartRaidTask) {
                System.out.println("Conversation ended because there's something more important to do!");
                return false;
            }
        }

        // Additional criteria 3: conversation ends when it's time to sleep.
        boolean timeToSleep = villagerEntity.getWorld().getRegistryKey().equals(World.OVERWORLD) &&  // In the Overworld
                                villagerEntity.getWorld().getTimeOfDay() % 24000 >= 12000;  // Nighttime
        if (timeToSleep) {
            System.out.println("Conversation ended because it's time to sleep!");
            return false;
        }

        // Additional criteria 4: conversation ends when there is a raid

        boolean activeRaid = brain.hasActivity(Activity.PRE_RAID) || brain.hasActivity(Activity.RAID);
        if (activeRaid) {
            System.out.println("Conversation ended because there is a raid!");
            return false;
        }

        return true;
    }

    /**
     * Actual things to do while running the task. Simply calls {@linkplain #followPlayer followPlayer}, which does the actual heavy-lifting.
     */
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        this.followPlayer(villagerEntity);
    }

    /**
     * Stuff to do when task terminates. Forgets walk target and look target, and sets IN_CONVERSATION state to false.
     */
    protected void finishRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        Brain<?> brain = villagerEntity.getBrain();
        brain.forget(MemoryModuleType.WALK_TARGET);
        brain.forget(MemoryModuleType.LOOK_TARGET);
        endConversation(villagerEntity);  // Set IN_CONVERSATION state to false.
    }

    /**
     * Stuff to do everytime {@linkplain #shouldKeepRunning shouldKeepRunning} is evaluated and returns true. Same as
     * {@linkplain #run run}.
     */
    protected void keepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        this.followPlayer(villagerEntity);
    }

    /**
     * Checks whether task time is exceeded. By simply making it return false, we make the task run indefinitely as
     * long as {@linkplain #shouldKeepRunning shouldKeepRunning} tells the task to keep running.
     */
    @Override
    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    /**
     * Makes the villager look at their conversation target and follow them.
     */
    private void followPlayer(VillagerEntity villager) {
        Brain<?> brain = villager.getBrain();
        brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(this.conversationTarget, false), this.speed, 2));
        brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(this.conversationTarget, true));
    }
}
