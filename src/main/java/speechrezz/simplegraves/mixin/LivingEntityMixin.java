package speechrezz.simplegraves.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import speechrezz.simplegraves.SimpleGraves;

@Mixin(PlayerEntity.class)
public abstract class LivingEntityMixin extends LivingEntity {
	protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "net.minecraft.entity.player.PlayerInventory.dropAll()V"))
	private void dropAll(PlayerInventory inventory) {
		World world = this.getWorld();
		if (world.isClient) return;

		if (SimpleGraves.placeGravestone(world, inventory)) {
			// Clear player inventory, XP, etc. so they don't drop anything.
			inventory.clear();
			inventory.player.experienceLevel = 0;
			inventory.player.totalExperience = 0;
			inventory.player.experienceProgress = 0.f;
		}
		else {
			inventory.dropAll();
		}
	}
}