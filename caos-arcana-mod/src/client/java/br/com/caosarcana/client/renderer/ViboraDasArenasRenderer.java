package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.ViboraDasArenasModel;
import br.com.caosarcana.common.entity.ViboraDasArenasEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class ViboraDasArenasRenderer
        extends MobRenderer<ViboraDasArenasEntity, ViboraDasArenasModel> {
    public ViboraDasArenasRenderer(EntityRendererProvider.Context context) {
        super(context,
                new ViboraDasArenasModel(context.bakeLayer(ViboraDasArenasModel.LAYER_LOCATION)),
                0.55F);
    }

    @Override
    public ResourceLocation getTextureLocation(ViboraDasArenasEntity entity) {
        return ViboraDasArenasModel.TEXTURE_LOCATION;
    }
}
