package speechrezz.simplegraves;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import speechrezz.simplegraves.entity.ModBlockEntities;
import speechrezz.simplegraves.renderer.GravestoneBlockEntityRenderer;

public class SimpleGravesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(ModBlockEntities.COUNTER_BLOCK_ENTITY, (context) -> new GravestoneBlockEntityRenderer());
	}
}