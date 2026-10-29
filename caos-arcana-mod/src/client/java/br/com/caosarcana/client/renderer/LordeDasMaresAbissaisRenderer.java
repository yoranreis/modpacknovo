package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.LordeDasMaresAbissaisModel;
import br.com.caosarcana.common.entity.LordeDasMaresAbissaisEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class LordeDasMaresAbissaisRenderer extends MobRenderer<LordeDasMaresAbissaisEntity, LordeDasMaresAbissaisModel> {
    public LordeDasMaresAbissaisRenderer(EntityRendererProvider.Context context) {
        super(context, new LordeDasMaresAbissaisModel(context.bakeLayer(LordeDasMaresAbissaisModel.LAYER_LOCATION)), 0.85F);
    }

    @Override
    protected void scale(LordeDasMaresAbissaisEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.22F, 1.22F, 1.22F);
    }

    @Override
    public ResourceLocation getTextureLocation(LordeDasMaresAbissaisEntity entity) {
        return LordeDasMaresAbissaisModel.TEXTURE_LOCATION;
    }
}
