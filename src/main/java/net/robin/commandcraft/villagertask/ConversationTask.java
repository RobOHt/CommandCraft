package net.robin.commandcraft.villagertask;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
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
     * ShouldRun checks whether the villagerEntity should start ConversationTask by checking if
     * <ul>
     *     <li>the <code>isInConversation</code> flag is up;</li>
     *     <li>the villager is alive;</li>
     *     <li>the conversation target is not <code>null</code>;</li>
     *     <li>and the villager is not asleep;.</li>
     * </ul>
     *
     * @param serverWorld the current server world
     * @param villagerEntity the current villager
     * @return whether the current task should initiate
     */
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        // Get the UUID of the player that villagerEntity is talking to
        String playerEntityUUID = getState(villagerEntity, VillagerState.CONVERSATION_PARTNER, String.class);

        // Get that playerEntity by searching for its UUID in the world the villager is currently living in
        if (playerEntityUUID == null || playerEntityUUID == "") return false;  // An extra check to prevent crash
        this.world = villagerEntity.getWorld();
        try {
            this.conversationTarget = this.world.getPlayerByUuid(UUID.fromString(playerEntityUUID));
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());  // If somehow the player UUID is invalid, we handle it more gracefully.
        }

        // Check for start conditions
        boolean shouldStartConversation = isInConversation(villagerEntity)
                && villagerEntity.isAlive()
                && conversationTarget != null
                && !villagerEntity.isSleeping();
        if (shouldStartConversation) {
            System.out.println("Conversation began.");
        }
        return shouldStartConversation;
    }

    /**
     * Checks whether ConversationTask should continue for the villagerEntity by checking if:
     * <ul>
     *     <li>the <code>isInConversation</code> flag is up; </li>
     *     <li>and the extra conditions defined by {@linkplain #taskIsAvailable continueTask} are also met.</li>
     * </ul>
     * @param serverWorld the current server world
     * @param villagerEntity the current villager
     * @param l time. Not actually used.
     * @return whether the current task should continue.
     */
    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        return isInConversation(villagerEntity) && taskIsAvailable(villagerEntity, this.conversationTarget);
    }

    /**
     * Checks whether the ConversationTask is available for continuation.
     * It returns <code>true</code> unless:
     * <ul>
     *   <li>The villager is dead, the conversation target is <code>null</code>, or the villager is sleeping;</li>
     *   <li>The conversation target is too far away;</li>
     *   <li>The villager is panicking, the bell is ringing, or there is an impending raid;</li>
     *   <li>The villager is about to sleep;</li>
     *   <li>There is an active raid.</li>
     * </ul>
     *
     * @param villagerEntity The villager in question.
     * @param conversationTarget The entity attempting to start the conversation.
     * @return <code>true</code> if the conversation should start or continue; <code>false</code> otherwise.
     */
    public static boolean taskIsAvailable(VillagerEntity villagerEntity, PlayerEntity conversationTarget) {
        // shouldKeepConversation holds base continuation criteria.
        boolean shouldKeepConversation = villagerEntity.isAlive()
                && conversationTarget != null
                && !villagerEntity.isSleeping();
        if (!shouldKeepConversation) {
            System.out.println("Conversation ended naturally.");
            villagerEntity.playAmbientSound();
            return false;
        }

        // Additional criteria 1: conversation ends when player goes too far away.
        if (villagerEntity.squaredDistanceTo(conversationTarget) > 25.0) {
            System.out.println("Conversation ended because the player is too far away.");
            villagerEntity.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.5F, villagerEntity.getSoundPitch());
            return false;
        }

        // Additional criteria 2: conversation ends when the villager is panicking, the bell is ringing, or if there is a raid.
        Brain<?> brain = villagerEntity.getBrain();
        for (Task<?> task : brain.getRunningTasks()) {
            if (task instanceof PanicTask || task instanceof HideWhenBellRingsTask || task instanceof StartRaidTask) {
                System.out.println("Conversation ended because there's something more important to do!");
                villagerEntity.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.5F, villagerEntity.getSoundPitch());
                conversationTarget.sendMessage(Text.literal("I got to go!"), true);
                return false;
            }
        }

        // Additional criteria 3: conversation ends when it's time to sleep.
        boolean timeToSleep = villagerEntity.getWorld().getRegistryKey().equals(World.OVERWORLD) &&  // In the Overworld
                villagerEntity.getWorld().getTimeOfDay() % 24000 >= 12000;  // Nighttime
        if (timeToSleep) {
            System.out.println("Conversation ended because it's time to sleep!");
            villagerEntity.playAmbientSound();
            conversationTarget.sendMessage(Text.literal("Shh! It's dark out there!"), true);
            return false;
        }

        // Additional criteria 4: conversation ends when there is a raid
        boolean activeRaid = brain.hasActivity(Activity.PRE_RAID) || brain.hasActivity(Activity.RAID);
        if (activeRaid) {
            System.out.println("Conversation ended because there is a raid!");
            villagerEntity.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.5F, villagerEntity.getSoundPitch());
            conversationTarget.sendMessage(Text.literal("RAID! I got to go!!!"), true);
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
     * Stuff to do when task terminates. Forgets walk target and look target, and sets <code>IN_CONVERSATION</code> state to false.
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
