package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.GolemDeGeloModel;
import br.com.caosarcana.common.entity.GolemDeGeloEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class GolemDeGeloRenderer extends MobRenderer<GolemDeGeloEntity, GolemDeGeloModel> {
    public GolemDeGeloRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemDeGeloModel(context.bakeLayer(GolemDeGeloModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(GolemDeGeloEntity entity) {
        return GolemDeGeloModel.TEXTURE_LOCATION;
    }
}
