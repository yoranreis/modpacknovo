package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.AranhaCascaSoltaModel;
import br.com.caosarcana.common.entity.AranhaCascaSoltaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class AranhaCascaSoltaRenderer
        extends MobRenderer<AranhaCascaSoltaEntity, AranhaCascaSoltaModel> {
    public AranhaCascaSoltaRenderer(EntityRendererProvider.Context context) {
        super(context, new AranhaCascaSoltaModel(
                context.bakeLayer(AranhaCascaSoltaModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(AranhaCascaSoltaEntity entity) {
        return AranhaCascaSoltaModel.TEXTURE_LOCATION;
    }
}
