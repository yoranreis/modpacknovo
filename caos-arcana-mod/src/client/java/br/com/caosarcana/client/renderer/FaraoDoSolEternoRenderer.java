package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.FaraoDoSolEternoModel;
import br.com.caosarcana.common.entity.FaraoDoSolEternoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class FaraoDoSolEternoRenderer
        extends MobRenderer<FaraoDoSolEternoEntity, FaraoDoSolEternoModel> {
    public FaraoDoSolEternoRenderer(EntityRendererProvider.Context context) {
        super(context,
                new FaraoDoSolEternoModel(context.bakeLayer(FaraoDoSolEternoModel.LAYER_LOCATION)),
                0.85F);
    }

    @Override
    protected void scale(FaraoDoSolEternoEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(FaraoDoSolEternoEntity entity) {
        return FaraoDoSolEternoModel.TEXTURE_LOCATION;
    }
}
