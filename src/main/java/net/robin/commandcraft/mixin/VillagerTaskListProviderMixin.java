package net.robin.commandcraft.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.passive.VillagerEntity;
import net.robin.commandcraft.villagertask.ConversationTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTaskListProvider.class)
public class VillagerTaskListProviderMixin {

    /**
     * Injection of our custom ConversationTask into createCoreTasks. This is done by replacing the whole return statement
     * with a newly built core task list that is the old core task list plus our new task.
     * @param cir Callback Info Returnable. This holds the return value of createCoreTasks, which happens to be the core tasks.
     */
    @Inject(
            method = "createCoreTasks",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void injectCustomTask(
            CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir
    ) {
        // Get the original list of tasks
        ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> originalTasks = cir.getReturnValue();

        // Create a new ImmutableList with your custom task added
        ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> newTasks = ImmutableList.<Pair<Integer, ? extends Task<? super VillagerEntity>>>builder()
                .addAll(originalTasks)
                .add(Pair.of(0, new ConversationTask())) // Added ConversationTask to core tasks with the highest priority (0 priority).
                .build();

        // Set the modified list as the return value
        cir.setReturnValue(newTasks);
    }
}