package br.com.caosarcana.client.renderer;

import br.com.caosarcana.client.model.EscaravelhoSolarModel;
import br.com.caosarcana.common.entity.EscaravelhoSolarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class EscaravelhoSolarRenderer
        extends MobRenderer<EscaravelhoSolarEntity, EscaravelhoSolarModel> {
    public EscaravelhoSolarRenderer(EntityRendererProvider.Context context) {
        super(context,
                new EscaravelhoSolarModel(context.bakeLayer(EscaravelhoSolarModel.LAYER_LOCATION)),
                0.75F);
    }

    @Override
    protected void scale(EscaravelhoSolarEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.15F, 1.15F, 1.15F);
    }

    @Override
    public ResourceLocation getTextureLocation(EscaravelhoSolarEntity entity) {
        return EscaravelhoSolarModel.TEXTURE_LOCATION;
    }
}
