package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.CarapacaBasalticaModel;
import br.com.caosarcana.common.entity.CarapacaBasalticaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class CarapacaBasalticaRenderer extends MobRenderer<CarapacaBasalticaEntity, CarapacaBasalticaModel> {
    public CarapacaBasalticaRenderer(EntityRendererProvider.Context context) {
        super(context, new CarapacaBasalticaModel(context.bakeLayer(CarapacaBasalticaModel.LAYER_LOCATION)), 0.55F);
    }

    @Override
    public ResourceLocation getTextureLocation(CarapacaBasalticaEntity entity) {
        return CarapacaBasalticaModel.TEXTURE_LOCATION;
    }
}
