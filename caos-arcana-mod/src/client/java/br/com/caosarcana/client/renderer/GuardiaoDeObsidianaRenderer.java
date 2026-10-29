package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.GuardiaoDeObsidianaModel;
import br.com.caosarcana.common.entity.GuardiaoDeObsidianaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class GuardiaoDeObsidianaRenderer extends MobRenderer<GuardiaoDeObsidianaEntity, GuardiaoDeObsidianaModel> {
    public GuardiaoDeObsidianaRenderer(EntityRendererProvider.Context context) {
        super(context, new GuardiaoDeObsidianaModel(context.bakeLayer(GuardiaoDeObsidianaModel.LAYER_LOCATION)), 0.65F);
    }

    @Override
    public ResourceLocation getTextureLocation(GuardiaoDeObsidianaEntity entity) {
        return GuardiaoDeObsidianaModel.TEXTURE_LOCATION;
    }
}
