package speechrezz.simplegraves.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import speechrezz.simplegraves.entity.custom.GravestoneBlockEntity;

import java.util.Map;

public class GravestoneBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final MapCodec<GravestoneBlock> CODEC = GravestoneBlock.createCodec(GravestoneBlock::new);
    private static final Map<Direction, VoxelShape> shapeByDirection = VoxelShapes.createFacingShapeMap(Block.createCuboidZShape(14.0, 16.0, 12.0, 14.0));

    public MapCodec<GravestoneBlock> getCodec() {
        return CODEC;
    }

    public GravestoneBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapeByDirection.get(state.get(FACING));
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return true;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GravestoneBlockEntity(pos, state);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        retrieveContents(world, pos, player);
        return super.onBreak(world, pos, state, player);
    }

    public void retrieveContents(World world, BlockPos pos, PlayerEntity player) {
        if(world.isClient) return;

        BlockEntity tempEntity = world.getBlockEntity(pos);
        if(!(tempEntity instanceof GravestoneBlockEntity blockEntity)) return;

        player.addExperience(blockEntity.getXp());
        ItemScatterer.spawn(world, pos, blockEntity.getItems());
    }
}
