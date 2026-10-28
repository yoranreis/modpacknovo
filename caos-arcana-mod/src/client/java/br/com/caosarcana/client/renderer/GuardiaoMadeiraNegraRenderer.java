package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.GuardiaoMadeiraNegraModel;
import br.com.caosarcana.common.entity.GuardiaoMadeiraNegraEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class GuardiaoMadeiraNegraRenderer
        extends MobRenderer<GuardiaoMadeiraNegraEntity, GuardiaoMadeiraNegraModel> {
    public GuardiaoMadeiraNegraRenderer(EntityRendererProvider.Context context) {
        super(context, new GuardiaoMadeiraNegraModel(
                context.bakeLayer(GuardiaoMadeiraNegraModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(GuardiaoMadeiraNegraEntity entity) {
        return GuardiaoMadeiraNegraModel.TEXTURE_LOCATION;
    }
}
