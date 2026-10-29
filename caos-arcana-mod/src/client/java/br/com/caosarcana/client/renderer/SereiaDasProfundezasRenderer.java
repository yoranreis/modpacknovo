package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.SereiaDasProfundezasModel;
import br.com.caosarcana.common.entity.SereiaDasProfundezasEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class SereiaDasProfundezasRenderer extends MobRenderer<SereiaDasProfundezasEntity, SereiaDasProfundezasModel> {
    public SereiaDasProfundezasRenderer(EntityRendererProvider.Context context) {
        super(context, new SereiaDasProfundezasModel(context.bakeLayer(SereiaDasProfundezasModel.LAYER_LOCATION)), 0.50F);
    }

    @Override
    public ResourceLocation getTextureLocation(SereiaDasProfundezasEntity entity) {
        return SereiaDasProfundezasModel.TEXTURE_LOCATION;
    }
}
