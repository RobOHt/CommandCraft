package net.robin.commandcraft.mixin;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.robin.commandcraft.villagerstate.VillagerStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {
	/**
	 * Modifies writeCustomDataToNbt so that everytime villager entity saves its own NBT data, it also saves our
	 * custom NBT data.
	 */
	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		VillagerStateManager.saveStates((VillagerEntity) (Object) this, nbt);
	}

	/**
	 * Modifies readCustomDataToNbt so that everytime villager entity reads its own NBT data, it also reads our
	 * custom NBT data.
	 */
	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		VillagerStateManager.loadStates((VillagerEntity) (Object) this, nbt);
	}
}