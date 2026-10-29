package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.GolemDeAreiaModel;
import br.com.caosarcana.common.entity.GolemDeAreiaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class GolemDeAreiaRenderer
        extends MobRenderer<GolemDeAreiaEntity, GolemDeAreiaModel> {
    public GolemDeAreiaRenderer(EntityRendererProvider.Context context) {
        super(context,
                new GolemDeAreiaModel(context.bakeLayer(GolemDeAreiaModel.LAYER_LOCATION)),
                0.85F);
    }

    @Override
    protected void scale(GolemDeAreiaEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.15F, 1.15F, 1.15F);
    }

    @Override
    public ResourceLocation getTextureLocation(GolemDeAreiaEntity entity) {
        return GolemDeAreiaModel.TEXTURE_LOCATION;
    }
}
