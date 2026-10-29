package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.MedusaBioluminescenteModel;
import br.com.caosarcana.common.entity.MedusaBioluminescenteEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class MedusaBioluminescenteRenderer extends MobRenderer<MedusaBioluminescenteEntity, MedusaBioluminescenteModel> {
    public MedusaBioluminescenteRenderer(EntityRendererProvider.Context context) {
        super(context, new MedusaBioluminescenteModel(context.bakeLayer(MedusaBioluminescenteModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(MedusaBioluminescenteEntity entity) {
        return MedusaBioluminescenteModel.TEXTURE_LOCATION;
    }
}
