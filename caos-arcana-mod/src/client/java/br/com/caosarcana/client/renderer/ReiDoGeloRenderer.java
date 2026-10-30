package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.ReiDoGeloModel;
import br.com.caosarcana.common.entity.ReiDoGeloEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class ReiDoGeloRenderer extends MobRenderer<ReiDoGeloEntity, ReiDoGeloModel> {
    public ReiDoGeloRenderer(EntityRendererProvider.Context context) {
        super(context, new ReiDoGeloModel(context.bakeLayer(ReiDoGeloModel.LAYER_LOCATION)), 0.9F);
    }

    @Override
    protected void scale(ReiDoGeloEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.28F, 1.28F, 1.28F);
    }

    @Override
    public ResourceLocation getTextureLocation(ReiDoGeloEntity entity) {
        return ReiDoGeloModel.TEXTURE_LOCATION;
    }
}
