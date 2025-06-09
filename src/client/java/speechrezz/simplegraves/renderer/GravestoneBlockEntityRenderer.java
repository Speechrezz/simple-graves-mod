package speechrezz.simplegraves.renderer;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import speechrezz.simplegraves.entity.custom.GravestoneBlockEntity;

public class GravestoneBlockEntityRenderer implements BlockEntityRenderer<GravestoneBlockEntity> {

    @Override
    public void render(GravestoneBlockEntity entity, float tickProgress, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            GameProfile profile = entity.getGraveOwner();
            if (profile == null) return;
            TextRenderer textRenderer = client.textRenderer;

            matrices.push();
            matrices.translate(0.5, 1.5, 0.5);

            float yaw = client.gameRenderer.getCamera().getYaw();
            float pitch = client.gameRenderer.getCamera().getPitch();
            
            // Rotate to face player
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
            
            matrices.scale(-0.025f, -0.025f, 0.025f);

            Text nameText = Text.literal(profile.getName());
            int textWidth = textRenderer.getWidth(nameText);

            textRenderer.draw(nameText, -textWidth / 2.0f, 0, 0xFFFFFF, false, 
                            matrices.peek().getPositionMatrix(), vertexConsumers, 
                            TextRenderer.TextLayerType.NORMAL, 0, light);

            matrices.pop();
    }
    
}
