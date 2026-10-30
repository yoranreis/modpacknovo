package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.CacadorDeGeloModel;
import br.com.caosarcana.common.entity.CacadorDeGeloEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class CacadorDeGeloRenderer extends MobRenderer<CacadorDeGeloEntity, CacadorDeGeloModel> {
    public CacadorDeGeloRenderer(EntityRendererProvider.Context context) {
        super(context, new CacadorDeGeloModel(context.bakeLayer(CacadorDeGeloModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CacadorDeGeloEntity entity) {
        return CacadorDeGeloModel.TEXTURE_LOCATION;
    }
}
