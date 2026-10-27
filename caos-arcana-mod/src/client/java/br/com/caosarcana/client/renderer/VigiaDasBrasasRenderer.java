package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.VigiaDasBrasasModel;
import br.com.caosarcana.common.entity.VigiaDasBrasasEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class VigiaDasBrasasRenderer extends MobRenderer<VigiaDasBrasasEntity, VigiaDasBrasasModel> {
    public VigiaDasBrasasRenderer(EntityRendererProvider.Context context) {
        super(context, new VigiaDasBrasasModel(context.bakeLayer(VigiaDasBrasasModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(VigiaDasBrasasEntity entity) {
        return VigiaDasBrasasModel.TEXTURE_LOCATION;
    }
}
