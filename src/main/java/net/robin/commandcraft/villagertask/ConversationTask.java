package net.robin.commandcraft.villagertask;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import static net.robin.commandcraft.utils.VillagerUtils.endConversation;
import static net.robin.commandcraft.utils.VillagerUtils.isInConversation;


public class ConversationTask extends MultiTickTask<VillagerEntity> {
    private final float speed = 0.4F;  // Walking speed during conversation. This is purposefully slower than usual.
    private PlayerEntity conversationTarget;  // The player to talk to.

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
        PlayerEntity playerEntity = villagerEntity.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
        this.conversationTarget = playerEntity;
        boolean shouldStartConversation = villagerEntity.isAlive()
                                            && this.conversationTarget != null
                                            && isInConversation(villagerEntity);
        if (shouldStartConversation) {
            System.out.println("Starting conversation.");
        }
        return shouldStartConversation;
    }

    /**
     * Task continuation criteria. They are {@linkplain #shouldRun shouldRun} criteria plus a number of additional
     * criteria.
     */
    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        // shouldKeepConversation are base continuation criteria.
        boolean shouldKeepConversation = villagerEntity.isAlive()
                                            && this.conversationTarget != null
                                            && isInConversation(villagerEntity);
        // Addition criteria 1: conversation ends when player goes too far away.
        if (villagerEntity.squaredDistanceTo(this.conversationTarget) > 25.0) {
            System.out.println("Conversation ended because the player is too far away.");
            return false;
        }
        return shouldKeepConversation;
    }

    /**
     * Actual things to do while running the task. Simply calls {@linkplain #update update}, which does the actual heavy-lifting.
     */
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        this.update(villagerEntity);
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
        this.update(villagerEntity);
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
    private void update(VillagerEntity villager) {
        Brain<?> brain = villager.getBrain();
        brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(this.conversationTarget, false), this.speed, 2));
        brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(this.conversationTarget, true));
    }
}
