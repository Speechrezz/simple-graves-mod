package com.speechrezz.simple_gravestones.mixin;

import com.speechrezz.simple_gravestones.registry.GravestoneBlock;
import com.speechrezz.simple_gravestones.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class LivingEntityMixin extends LivingEntity {

    protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    // When player dies and is about to lose his inventory
    @Inject(at = @At("HEAD"), method = "dropInventory")
    protected void dropInventory(CallbackInfo info) {
        System.out.println("DEBUG - dropInventory() called");

        // Get position of player
        BlockPos blockPos = new BlockPos(this.getPos());



        // Get the PlayerEntity of the player who just died
        PlayerEntity deadGuy = this.world.getClosestPlayer(this,10f);
        // Get BlockState of a grave block
        BlockState blockState = ModBlocks.GRAVE_BLOCK.getDefaultState().with(GravestoneBlock.FACING, deadGuy.getHorizontalFacing().getOpposite());

        //System.out.println("DEBUG - Player Slot 0: " + deadGuy.getInventory().getStack(0).toString());

        if (deadGuy != null) {
            if (!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
                blockPos = findBestSpot(this.world, blockPos);
                blockPos = addDirtBlock(this.world, blockPos);

                this.world.setBlockState(blockPos, blockState, Block.NOTIFY_ALL);
                swapInventory(deadGuy, (Inventory) world.getBlockEntity(blockPos));

                //System.out.println("DEBUG - Gravestone Block Inventory: " + ((Inventory) this.world.getBlockEntity(blockPos)).getStack(0).toString());
                deadGuy.getInventory().clear();
            }

            if (!this.world.isClient) {
                deadGuy.sendMessage(new LiteralText("You died at: " + blockPos.toShortString()), false);
            }
        }
    }

    private void swapInventory(PlayerEntity deadGuy, Inventory targetInv){
        for (int i=0; i<deadGuy.getInventory().size(); i++){
            //System.out.println(deadGuy.getInventory().getStack(i).toString());
            targetInv.setStack(i, deadGuy.getInventory().getStack(i));
        }
    }

    private BlockPos findBestSpot(World world, BlockPos currentPos){
        System.out.println("DEBUG - Bottom Y: " + world.getBottomY() + ", Top Y: " + world.getTopY());

        BlockPos initialPos = currentPos;

        if (currentPos.getY() <= world.getBottomY()){
            currentPos = currentPos.withY(world.getBottomY() + 1);
        }

        System.out.println("DEBUG - isAir(): " + world.getBlockState(currentPos).isAir() + ", isInBuildLimit(): " + world.isInBuildLimit(currentPos) + ", getY(): " + currentPos.getY());
        while (!world.getBlockState(currentPos).isAir() && world.isInBuildLimit(currentPos)){
            System.out.println("DEBUG - isAir(): " + world.getBlockState(currentPos).isAir() + ", isInBuildLimit(): " + world.isInBuildLimit(currentPos) + ", getY(): " + currentPos.getY());
            currentPos = currentPos.up();
        }
        // If could not find empty space while checking all spaces above
        if (!world.isInBuildLimit(currentPos)){
            // If the initial position was good enough, just use that
            if (world.isInBuildLimit(initialPos)){
                return initialPos;
            }
            // While the current position is above the world build limit, keeping looping downward
            if (currentPos.getY() >= world.getTopY()){
                currentPos = currentPos.withY(world.getTopY() - 1);
            }

            while (!world.getBlockState(currentPos).isAir() && world.isInBuildLimit(currentPos)){
                System.out.println("DEBUG - isAir(): " + world.getBlockState(currentPos).isAir() + ", isInBuildLimit(): " + world.isInBuildLimit(currentPos) + ", getY(): " + currentPos.getY());
                currentPos = currentPos.down();
            }

            // If it STILL couldn't find an empty space, just set the y value to something arbitrary
            if (!world.isInBuildLimit(currentPos)){
                return initialPos.withY(100);
            }

        }

        return currentPos;
    }

    private BlockPos addDirtBlock(World world, BlockPos currentPos){

        //BlockState blockStateCurrent = world.getBlockState(currentPos);

        // For checking right below the player
        BlockPos blockPosBelow = currentPos.down();
        BlockState blockStateBelow = world.getBlockState(blockPosBelow);

        if (blockStateBelow.isAir()) {
            world.setBlockState(blockPosBelow, Blocks.DIRT.getDefaultState(), Block.NOTIFY_ALL);
        }
        /**
        else if (!isWithinWorldLimit(world, blockPosBelow) && isWithinWorldLimit(world, currentPos) && blockStateCurrent.isAir()){
            world.setBlockState(currentPos, Blocks.DIRT.getDefaultState(), Block.NOTIFY_ALL);
            return currentPos.up();
        } **/

        return currentPos;
    }

    private boolean isWithinWorldLimit(World world, BlockPos currentPos){
        if (currentPos.getY() >= world.getBottomY() && currentPos.getY() <= world.getTopY()){
            return true;
        }
        return false;
    }

}