package speechrezz.simplegraves.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import speechrezz.simplegraves.SimpleGraves;
import speechrezz.simplegraves.block.ModBlocks;
import speechrezz.simplegraves.entity.custom.GravestoneBlockEntity;

@Mixin(PlayerEntity.class)
public abstract class LivingEntityMixin extends LivingEntity {
	protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "net.minecraft.entity.player.PlayerInventory.dropAll()V"))
	private void dropAll(PlayerInventory inventory) {
		World world = this.getWorld();
		if (world.isClient) return;

		BlockPos gravePos = this.getBlockPos(); // TODO: Find better grave position if needed

		// Place gravestone
		BlockState graveState = ModBlocks.GRAVESTONE.getDefaultState().with(Properties.HORIZONTAL_FACING, inventory.player.getHorizontalFacing().getOpposite());
		world.setBlockState(gravePos, graveState);

		// Save player inventory, XP, etc.
		GravestoneBlockEntity gravestoneBlockEntity = new GravestoneBlockEntity(gravePos, graveState);
		DefaultedList<ItemStack> graveItems = DefaultedList.of();
		for (ItemStack stack : inventory)
			graveItems.add(stack);

		gravestoneBlockEntity.setItems(graveItems);
		gravestoneBlockEntity.setGraveOwner(inventory.player.getGameProfile());
		gravestoneBlockEntity.setXp(inventory.player.totalExperience);
		gravestoneBlockEntity.markDirty();
		world.addBlockEntity(gravestoneBlockEntity);

		// Clear player inventory, XP, etc. so they don't drop anything.
		inventory.clear();
		inventory.player.experienceLevel = 0;
		inventory.player.totalExperience = 0;
		inventory.player.experienceProgress = 0.f;

		SimpleGraves.LOGGER.info("[SimpleGraves] Gravestone placed at ({}, {}, {})", gravePos.getX(), gravePos.getY(), gravePos.getZ());
	}
}