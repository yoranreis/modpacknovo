package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.MineiroOcoModel;
import br.com.caosarcana.common.entity.MineiroOcoEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class MineiroOcoRenderer extends MobRenderer<MineiroOcoEntity, MineiroOcoModel> {
    public MineiroOcoRenderer(EntityRendererProvider.Context context) {
        super(context,
                new MineiroOcoModel(context.bakeLayer(MineiroOcoModel.LAYER_LOCATION)),
                0.45F);
        addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MineiroOcoEntity entity) {
        return MineiroOcoModel.TEXTURE_LOCATION;
    }
}
