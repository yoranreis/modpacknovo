package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.CaoFumeganteModel;
import br.com.caosarcana.common.entity.CaoFumeganteEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class CaoFumeganteRenderer
        extends MobRenderer<CaoFumeganteEntity, CaoFumeganteModel> {
    public CaoFumeganteRenderer(EntityRendererProvider.Context context) {
        super(context,
                new CaoFumeganteModel(context.bakeLayer(CaoFumeganteModel.LAYER_LOCATION)),
                0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(CaoFumeganteEntity entity) {
        return CaoFumeganteModel.TEXTURE_LOCATION;
    }
}
