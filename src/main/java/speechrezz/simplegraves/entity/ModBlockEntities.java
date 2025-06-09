package speechrezz.simplegraves.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import speechrezz.simplegraves.SimpleGraves;
import speechrezz.simplegraves.block.ModBlocks;
import speechrezz.simplegraves.entity.custom.GravestoneBlockEntity;

public class ModBlockEntities {
    public static final BlockEntityType<GravestoneBlockEntity> COUNTER_BLOCK_ENTITY =
            register("gravestone", GravestoneBlockEntity::new, ModBlocks.GRAVESTONE);

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        Identifier id = Identifier.of(SimpleGraves.MOD_ID, name);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static void initialize() {}
}
