package net.robin.commandcraft.villagerstate;

public enum VillagerState {
    IN_CONVERSATION, // Boolean. True when a villager is talking to a player.
    AGE,             // Integer. Days a villager had lived / 365
    LLM_MEMORY       // String. The villager's large language model memory. Essentially their own "story".
}