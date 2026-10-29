package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.TragadorDoAbismoModel;
import br.com.caosarcana.common.entity.TragadorDoAbismoEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class TragadorDoAbismoRenderer extends MobRenderer<TragadorDoAbismoEntity, TragadorDoAbismoModel> {
    public TragadorDoAbismoRenderer(EntityRendererProvider.Context context) {
        super(context, new TragadorDoAbismoModel(context.bakeLayer(TragadorDoAbismoModel.LAYER_LOCATION)), 0.55F);
    }

    @Override
    public ResourceLocation getTextureLocation(TragadorDoAbismoEntity entity) {
        return TragadorDoAbismoModel.TEXTURE_LOCATION;
    }
}
