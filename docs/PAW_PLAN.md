> **🚧 Work in progress.** This is a draft plan for adding Program As Weights (PAW) compiled villager behaviours to CommandCraft. Nothing here is implemented yet, and the plan will change.

# Program As Weights × Minecraft — Current Plan

## Goal
Use the PAW compiler (arXiv 2607.02512) to turn plain-English villager behaviour specs into LoRA adapters on a frozen Qwen3-0.6B, running locally inside a Minecraft mod.

## Where things stand
- PAW is public: hosted compile API, local llama.cpp runtime, MIT SDK, HF weights + FuzzyBench.
- First test done (CPU sandbox, test code kept outside this repo). Compiled adapter vs plain 0.6B prompt:

| Task | PAW (ft) | Plain 0.6B |
|---|---|---|
| Chat intent | 39/40 | 24/40 |
| Next activity | 28/30 | 10/30 |
| Trade price | 21/25 | 2/25 |
| Commands | 29/30 | 22/30 |

- Design doc: Robin is writing it himself (proposes a single adapter).

## Version 1 — Compiled villager functions
1. Pick the base mod: CommandCraft (LLM villagers, Flask server) or VillagersPlus (brain/mood).
2. Generate ~200 test cases per function with a big model (Opus) instead of hand labels.
3. Compile one adapter from a single spec-in-prompt; compare against plain 0.6B and the big model.
4. Wire into the mod: Java sends facts, adapter returns the action, local runtime with LoRA hot-swap.

## Version 2 — Grounded villagers with compiled memory
- **Grounded pipeline:** Java facts with IDs → adapter picks a legal action (constrained decoding) → adapter explains, citing fact IDs → verifier.
- **Memory:** consolidate asynchronously during play (not during sleep). Big model distils logs into short rules appended to the spec → recompile → probe/regression gate → hot-swap with rollback. Recent uncompiled memories are passed as input (fast/slow store).
- Open problem: what counts as a memory, and how to verify the adapter learned exactly that.

## Stretch ideas
Skill sharing via adapter files · reward-trained compiler · personality search (MCTS) · adapter debugger · compare with "Sparks of Weight-Space Programming" (Rikiya Takehi).

## Infra
Learn GCP: compile on the hosted API, serve adapters on CPU Cloud Run deployed from GitHub. Free credit is used up, so stay in the always-free tier.

## Open questions
- Runtime with LoRA hot-swap (llama.cpp?)
- Robin's local hardware
- CommandCraft vs VillagersPlus as the base
- Involve Prof. Deng early?
- What a compiler is, and which field this project belongs to
