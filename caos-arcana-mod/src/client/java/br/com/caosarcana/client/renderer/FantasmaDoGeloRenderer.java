package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.FantasmaDoGeloModel;
import br.com.caosarcana.common.entity.FantasmaDoGeloEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class FantasmaDoGeloRenderer extends MobRenderer<FantasmaDoGeloEntity, FantasmaDoGeloModel> {
    public FantasmaDoGeloRenderer(EntityRendererProvider.Context context) {
        super(context, new FantasmaDoGeloModel(context.bakeLayer(FantasmaDoGeloModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(FantasmaDoGeloEntity entity) {
        return FantasmaDoGeloModel.TEXTURE_LOCATION;
    }
}
