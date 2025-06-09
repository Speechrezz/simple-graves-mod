package speechrezz.simplegraves.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import speechrezz.simplegraves.SimpleGraves;
import speechrezz.simplegraves.block.ModBlocks;
import speechrezz.simplegraves.entity.custom.GravestoneBlockEntity;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class LivingEntityMixin extends LivingEntity {
	@Shadow @Final private PlayerInventory inventory;

	protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "net.minecraft.entity.player.PlayerInventory.dropAll()V"))
	private void dropAll(PlayerInventory inventory) {
		World world = this.getWorld();
		if (world.isClient) return;

		if (placeGravestone(world, inventory)) {
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

	private static boolean placeGravestone(World world, PlayerInventory inventory) {
		Optional<BlockPos> gravePosOptional = findGravestonePlacementPosition(world, inventory.player.getBlockPos());
		if (gravePosOptional.isEmpty()) return false;
		BlockPos gravePos = gravePosOptional.get();

		// Place gravestone
		BlockState graveState = ModBlocks.GRAVESTONE.getDefaultState().with(Properties.HORIZONTAL_FACING, inventory.player.getHorizontalFacing().getOpposite());
		if (!world.setBlockState(gravePos, graveState)) return false;
		tryPlaceDirt(world, gravePos.down(1));
		tryPlaceDirt(world, gravePos.down(1).offset(inventory.player.getHorizontalFacing().getOpposite(), 1));

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

		SimpleGraves.LOGGER.info("[SimpleGraves] Gravestone placed at ({}, {}, {})", gravePos.getX(), gravePos.getY(), gravePos.getZ());
		return true;
	}

	private static boolean canPlaceGravestone(World world, BlockPos blockPos) {
		if(world.getBlockEntity(blockPos) != null) return false;
		Block block = world.getBlockState(blockPos).getBlock();

		boolean isWithinWorldBounds = blockPos.getY() <= world.getDimension().height() - world.getDimension().minY();
		boolean isAir = block == Blocks.AIR || block == Blocks.WATER;

		return isWithinWorldBounds && isAir;
	}

	private static Optional<BlockPos> findGravestonePlacementPosition(World world, BlockPos startPos) {
		startPos = startPos.withY(Math.max(world.getDimension().minY() + 1, startPos.getY())); // Clamp Y

		for (BlockPos blockPos : BlockPos.iterateOutwards(startPos, 5, 6, 5))
			if (canPlaceGravestone(world, blockPos))
				return Optional.of(blockPos);

		return Optional.empty();
	}

	private static void tryPlaceDirt(World world, BlockPos dirtPos) {
		Block block = world.getBlockState(dirtPos).getBlock();
		if (block == Blocks.AIR || block == Blocks.GRASS_BLOCK)
			world.setBlockState(dirtPos, Blocks.DIRT.getDefaultState());
	}
}