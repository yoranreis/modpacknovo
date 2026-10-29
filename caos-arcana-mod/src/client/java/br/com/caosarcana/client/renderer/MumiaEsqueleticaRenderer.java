package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.MumiaEsqueleticaModel;
import br.com.caosarcana.common.entity.MumiaEsqueleticaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class MumiaEsqueleticaRenderer
        extends MobRenderer<MumiaEsqueleticaEntity, MumiaEsqueleticaModel> {
    public MumiaEsqueleticaRenderer(EntityRendererProvider.Context context) {
        super(context,
                new MumiaEsqueleticaModel(context.bakeLayer(MumiaEsqueleticaModel.LAYER_LOCATION)),
                0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(MumiaEsqueleticaEntity entity) {
        return MumiaEsqueleticaModel.TEXTURE_LOCATION;
    }
}
