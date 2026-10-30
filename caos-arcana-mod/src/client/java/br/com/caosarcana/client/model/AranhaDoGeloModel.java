package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.AranhaDoGeloEntity;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class AranhaDoGeloModel extends EntityModel<AranhaDoGeloEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("aranha_do_gelo"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/muralha_de_gelo/aranha_do_gelo.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart[] legs = new ModelPart[8];

    public AranhaDoGeloModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        for (int i = 0; i < 8; i++) {
            this.legs[i] = root.getChild("leg_" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Cabeça com múltiplos olhos azul neon e quelíceras de cristal de gelo
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 7.0F, 8.0F)
                        .texOffs(34, 0).addBox(-3.0F, 1.0F, -10.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(44, 0).addBox(1.0F, 1.0F, -10.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 15.0F, -3.0F));

        // 2. Cefalotórax e Abdômen com grande aglomerado de cristais de gelo no dorso
        root.addOrReplaceChild("thorax",
                CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        root.addOrReplaceChild("abdomen",
                CubeListBuilder.create()
                        .texOffs(26, 18).addBox(-5.0F, -4.0F, 0.0F, 10.0F, 8.0F, 11.0F)
                        .texOffs(70, 18).addBox(-3.0F, -10.0F, 2.0F, 6.0F, 7.0F, 6.0F)
                        .texOffs(96, 18).addBox(-2.0F, -12.0F, 4.0F, 4.0F, 9.0F, 4.0F),
                PartPose.offset(0.0F, 15.0F, 3.0F));

        // 3. Oito patas encouraçadas com pontas de cristal de gelo azul
        float[] zOffsets = new float[]{-2.0F, -1.0F, 1.0F, 2.5F};
        for (int pair = 0; pair < 4; pair++) {
            int leftIdx = pair * 2;
            int rightIdx = pair * 2 + 1;
            root.addOrReplaceChild("leg_" + leftIdx,
                    CubeListBuilder.create()
                            .texOffs(0, 40 + leftIdx * 6).addBox(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F),
                    PartPose.offset(-4.0F, 15.0F, zOffsets[pair]));

            root.addOrReplaceChild("leg_" + rightIdx,
                    CubeListBuilder.create()
                            .texOffs(0, 40 + rightIdx * 6).addBox(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F),
                    PartPose.offset(4.0F, 15.0F, zOffsets[pair]));
        }

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(AranhaDoGeloEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float baseZ = 0.7853982F;
        float[] baseY = new float[]{0.7853982F, 0.3926991F, -0.3926991F, -0.7853982F};
        for (int pair = 0; pair < 4; pair++) {
            ModelPart left = this.legs[pair * 2];
            ModelPart right = this.legs[pair * 2 + 1];
            float swingY = Mth.cos(limbSwing * 0.6662F * 2.0F + pair * 0.8F) * 0.4F * limbSwingAmount;
            float swingZ = Math.abs(Mth.sin(limbSwing * 0.6662F + pair * 0.8F) * 0.4F) * limbSwingAmount;

            left.zRot = -baseZ + swingZ;
            right.zRot = baseZ - swingZ;
            left.yRot = baseY[pair] + swingY;
            right.yRot = -baseY[pair] - swingY;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
