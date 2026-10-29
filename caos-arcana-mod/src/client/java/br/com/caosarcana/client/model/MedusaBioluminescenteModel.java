package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.MedusaBioluminescenteEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class MedusaBioluminescenteModel extends EntityModel<MedusaBioluminescenteEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("medusa_bioluminescente"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/recife_coral_abissal/medusa_bioluminescente.png");

    private final ModelPart root;
    private final ModelPart bell;
    private final ModelPart innerCore;
    private final ModelPart[] tentaclesUpper = new ModelPart[6];
    private final ModelPart[] tentaclesLower = new ModelPart[6];

    public MedusaBioluminescenteModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        this.bell = root.getChild("bell");
        this.innerCore = root.getChild("inner_core");
        for (int i = 0; i < 6; i++) {
            this.tentaclesUpper[i] = this.bell.getChild("tentacle_upper_" + i);
            this.tentaclesLower[i] = this.tentaclesUpper[i].getChild("tentacle_lower_" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Campânula translúcida ciano-lilás em camadas
        PartDefinition bell = root.addOrReplaceChild("bell",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 3.0F, 8.0F)
                        .texOffs(0, 14).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 7.0F, 12.0F)
                        .texOffs(0, 36).addBox(-7.0F, 1.0F, -7.0F, 14.0F, 2.0F, 14.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));

        // 2. Núcleo bioelétrico esférico pulsante no interior
        root.addOrReplaceChild("inner_core",
                CubeListBuilder.create()
                        .texOffs(60, 0).addBox(-3.0F, -4.5F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));

        // 3. Seis tentáculos bioluminescentes articulados
        float[][] offsets = new float[][]{
                {-3.8F, -3.5F},
                {3.8F, -3.5F},
                {-4.5F, 0.0F},
                {4.5F, 0.0F},
                {-3.8F, 3.5F},
                {3.8F, 3.5F}
        };

        for (int i = 0; i < 6; i++) {
            PartDefinition upper = bell.addOrReplaceChild("tentacle_upper_" + i,
                    CubeListBuilder.create()
                            .texOffs(i * 10, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                    PartPose.offset(offsets[i][0], 2.5F, offsets[i][1]));

            upper.addOrReplaceChild("tentacle_lower_" + i,
                    CubeListBuilder.create()
                            .texOffs(i * 10, 70).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                    PartPose.offset(0.0F, 6.8F, 0.0F));
        }

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(MedusaBioluminescenteEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float pulse = Mth.sin(ageInTicks * 0.14F) * 1.1F;
        this.bell.y = 7.0F + pulse;
        this.innerCore.y = 7.0F + pulse;
        this.innerCore.yRot = ageInTicks * 0.05F;

        for (int i = 0; i < 6; i++) {
            float phase = ageInTicks * 0.14F + i * 0.85F;
            this.tentaclesUpper[i].xRot = 0.18F + Mth.sin(phase) * 0.24F;
            this.tentaclesUpper[i].zRot = Mth.cos(phase) * 0.15F;
            this.tentaclesLower[i].xRot = 0.22F + Mth.sin(phase - 0.7F) * 0.30F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, 0.90F);
    }
}
