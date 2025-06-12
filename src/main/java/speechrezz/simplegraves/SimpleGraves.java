package speechrezz.simplegraves;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import speechrezz.simplegraves.block.ModBlocks;
import speechrezz.simplegraves.entity.ModBlockEntities;
import speechrezz.simplegraves.entity.custom.GravestoneBlockEntity;

import java.util.Optional;

public class SimpleGraves implements ModInitializer {
	public static final String MOD_ID = "simplegraves";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final GameRules.Key<GameRules.BooleanRule> PREVENT_GRAVE_ROBBING =
			GameRuleRegistry.register("preventGraveRobbing", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		LOGGER.info("Simple Graves Initialized");
	}

	public static boolean placeGravestone(World world, PlayerInventory inventory) {
		Optional<BlockPos> gravePosOptional = findGravestonePlacementPosition(world, inventory.player.getBlockPos());
		if (gravePosOptional.isEmpty()) return false;
		BlockPos gravePos = gravePosOptional.get();

		// Place gravestone
		BlockState graveState = ModBlocks.GRAVESTONE.getDefaultState()
				.with(Properties.HORIZONTAL_FACING, inventory.player.getHorizontalFacing().getOpposite())
				.with(Properties.WATERLOGGED, world.getFluidState(gravePos).isOf(Fluids.WATER));
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

		inventory.player.sendMessage(Text.translatable("text.gravestones.grave_coordinates", gravePos.getX(), gravePos.getY(), gravePos.getZ()), false);
		SimpleGraves.LOGGER.info("[SimpleGraves] Gravestone placed at ({})", gravePos.toShortString());
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