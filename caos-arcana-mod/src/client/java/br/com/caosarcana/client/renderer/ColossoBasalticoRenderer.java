package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.CarapacaBasalticaModel;
import br.com.caosarcana.common.entity.ColossoBasalticoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class ColossoBasalticoRenderer
        extends MobRenderer<ColossoBasalticoEntity, CarapacaBasalticaModel<ColossoBasalticoEntity>> {
    public ColossoBasalticoRenderer(EntityRendererProvider.Context context) {
        super(context,
                new CarapacaBasalticaModel<>(context.bakeLayer(CarapacaBasalticaModel.LAYER_LOCATION)),
                1.2F);
    }

    @Override
    protected void scale(ColossoBasalticoEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(3.6F, 2.3F, 2.1F);
    }

    @Override
    public ResourceLocation getTextureLocation(ColossoBasalticoEntity entity) {
        return CarapacaBasalticaModel.TEXTURE_LOCATION;
    }
}
