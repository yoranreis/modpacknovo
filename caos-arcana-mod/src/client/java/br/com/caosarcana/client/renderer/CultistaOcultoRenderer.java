package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.CultistaOcultoModel;
import br.com.caosarcana.common.entity.CultistaOcultoEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class CultistaOcultoRenderer extends MobRenderer<CultistaOcultoEntity, CultistaOcultoModel> {
    public CultistaOcultoRenderer(EntityRendererProvider.Context context) {
        super(context, new CultistaOcultoModel(context.bakeLayer(CultistaOcultoModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CultistaOcultoEntity entity) {
        return CultistaOcultoModel.TEXTURE_LOCATION;
    }
}
