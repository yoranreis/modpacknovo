package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.AnciaoDasSombrasModel;
import br.com.caosarcana.common.entity.AnciaoDasSombrasEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class AnciaoDasSombrasRenderer
        extends MobRenderer<AnciaoDasSombrasEntity, AnciaoDasSombrasModel> {
    public AnciaoDasSombrasRenderer(EntityRendererProvider.Context context) {
        super(context,
                new AnciaoDasSombrasModel(context.bakeLayer(AnciaoDasSombrasModel.LAYER_LOCATION)),
                1.1F);
    }

    @Override
    protected void scale(AnciaoDasSombrasEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.55F, 1.55F, 1.55F);
    }

    @Override
    public ResourceLocation getTextureLocation(AnciaoDasSombrasEntity entity) {
        return AnciaoDasSombrasModel.TEXTURE_LOCATION;
    }
}
