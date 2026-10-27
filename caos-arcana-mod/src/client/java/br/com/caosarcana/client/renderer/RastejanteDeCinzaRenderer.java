package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.RastejanteDeCinzaModel;
import br.com.caosarcana.common.entity.RastejanteDeCinzaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class RastejanteDeCinzaRenderer
        extends MobRenderer<RastejanteDeCinzaEntity, RastejanteDeCinzaModel> {
    public RastejanteDeCinzaRenderer(EntityRendererProvider.Context context) {
        super(context,
                new RastejanteDeCinzaModel(context.bakeLayer(RastejanteDeCinzaModel.LAYER_LOCATION)),
                0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(RastejanteDeCinzaEntity entity) {
        return RastejanteDeCinzaModel.TEXTURE_LOCATION;
    }
}
