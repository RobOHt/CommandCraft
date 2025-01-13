package net.robin.commandcraft.villagerstate;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class VillagerStateManager {
    // Use a WeakHashMap to store custom states for each VillagerEntity
    private static final Map<VillagerEntity, Map<VillagerState, Object>> VILLAGER_STATES = new WeakHashMap<>();

    /**
     * Get the custom states for a specific VillagerEntity.
     * If no states exist, a new Map is created and returned.
     */
    private static Map<VillagerState, Object> getStates(VillagerEntity villager) {
        return VILLAGER_STATES.computeIfAbsent(villager, k -> new HashMap<>());
    }

    /**
     * Set a custom state for a specific VillagerEntity.
     *
     * @param villager The VillagerEntity to modify.
     * @param state    The state key (from the VillagerState enum).
     * @param value    The value of the state (must match the expected type).
     * @throws IllegalArgumentException If the value type does not match the state's expected type.
     */
    public static void setState(VillagerEntity villager, VillagerState state, Object value) {
        // Validate the value type
        switch (state) {
            case IN_CONVERSATION:
                if (!(value instanceof Boolean)) {
                    throw new IllegalArgumentException("IN_CONVERSATION state must be a boolean.");
                }
                break;
            case AGE:
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException("AGE state must be an integer.");
                }
                break;
            case LLM_MEMORY:
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException("LLM_MEMORY state must be a string.");
                }
                break;
        }

        // Set the state
        getStates(villager).put(state, value);
    }

    /**
     * Get a specific state for a VillagerEntity.
     *
     * @param villager The VillagerEntity to query.
     * @param state    The state key (from the VillagerState enum).
     * @return The state value, or null if it doesn't exist.
     */
    public static <T> T getState(VillagerEntity villager, VillagerState state, Class<T> type) {
        Object value = getStates(villager).get(state);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    /**
     * Remove a specific state for a VillagerEntity.
     *
     * @param villager The VillagerEntity to modify.
     * @param state    The state key (from the VillagerState enum).
     */
    public static void removeState(VillagerEntity villager, VillagerState state) {
        getStates(villager).remove(state);
    }

    /**
     * Clear all custom states for a VillagerEntity.
     *
     * @param villager The VillagerEntity to clear.
     */
    public static void clearStates(VillagerEntity villager) {
        VILLAGER_STATES.remove(villager);
    }

    /**
     * Save custom states to NBT.
     *
     * @param villager The VillagerEntity to save.
     * @param nbt      The NBT compound to write to.
     */
    public static void saveStates(VillagerEntity villager, NbtCompound nbt) {
        Map<VillagerState, Object> states = getStates(villager);
        NbtCompound stateNbt = new NbtCompound();

        for (Map.Entry<VillagerState, Object> entry : states.entrySet()) {
            VillagerState state = entry.getKey();
            Object value = entry.getValue();

            switch (state) {
                case IN_CONVERSATION:
                    stateNbt.putBoolean(state.name(), (Boolean) value);
                    break;
                case AGE:
                    stateNbt.putInt(state.name(), (Integer) value);
                    break;
                case LLM_MEMORY:
                    stateNbt.putString(state.name(), (String) value);
                    break;
            }
        }

        nbt.put("CustomStates", stateNbt);
    }

    /**
     * Load custom states from NBT.
     *
     * @param villager The VillagerEntity to load.
     * @param nbt      The NBT compound to read from.
     */
    public static void loadStates(VillagerEntity villager, NbtCompound nbt) {
        if (nbt.contains("CustomStates", NbtElement.COMPOUND_TYPE)) {
            NbtCompound stateNbt = nbt.getCompound("CustomStates");

            for (VillagerState state : VillagerState.values()) {
                if (stateNbt.contains(state.name())) {
                    switch (state) {
                        case IN_CONVERSATION:
                            setState(villager, state, stateNbt.getBoolean(state.name()));
                            break;
                        case AGE:
                            setState(villager, state, stateNbt.getInt(state.name()));
                            break;
                        case LLM_MEMORY:
                            setState(villager, state, stateNbt.getString(state.name()));
                            break;
                    }
                }
            }
        }
    }
}