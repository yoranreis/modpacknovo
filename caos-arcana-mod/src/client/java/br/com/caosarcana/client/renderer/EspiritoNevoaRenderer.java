package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.EspiritoNevoaModel;
import br.com.caosarcana.common.entity.EspiritoNevoaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class EspiritoNevoaRenderer extends MobRenderer<EspiritoNevoaEntity, EspiritoNevoaModel> {
    public EspiritoNevoaRenderer(EntityRendererProvider.Context context) {
        super(context, new EspiritoNevoaModel(context.bakeLayer(EspiritoNevoaModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(EspiritoNevoaEntity entity) {
        return EspiritoNevoaModel.TEXTURE_LOCATION;
    }
}
