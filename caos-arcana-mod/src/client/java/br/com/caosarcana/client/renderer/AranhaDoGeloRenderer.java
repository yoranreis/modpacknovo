package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.AranhaDoGeloModel;
import br.com.caosarcana.common.entity.AranhaDoGeloEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class AranhaDoGeloRenderer extends MobRenderer<AranhaDoGeloEntity, AranhaDoGeloModel> {
    public AranhaDoGeloRenderer(EntityRendererProvider.Context context) {
        super(context, new AranhaDoGeloModel(context.bakeLayer(AranhaDoGeloModel.LAYER_LOCATION)), 0.65F);
    }

    @Override
    public ResourceLocation getTextureLocation(AranhaDoGeloEntity entity) {
        return AranhaDoGeloModel.TEXTURE_LOCATION;
    }
}
