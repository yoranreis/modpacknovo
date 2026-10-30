package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.GolemDeGeloEntity;
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
public final class GolemDeGeloModel extends EntityModel<GolemDeGeloEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("golem_de_gelo"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/muralha_de_gelo/golem_de_gelo.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GolemDeGeloModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Cabeça cúbica de gelo compacto
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        // 2. Torso maciço de blocos de gelo com núcleo ciano e cristais nos ombros
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-6.0F, 0.0F, -3.5F, 12.0F, 12.0F, 7.0F)
                        .texOffs(40, 20).addBox(-4.0F, 12.0F, -2.5F, 8.0F, 5.0F, 5.0F)
                        .texOffs(68, 20).addBox(-5.5F, -5.0F, 0.0F, 3.0F, 7.0F, 3.0F)
                        .texOffs(82, 20).addBox(2.5F, -5.0F, 0.0F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        // 3. Braço Direito maciço com espinho de cristal de gelo
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-5.5F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                        .texOffs(26, 42).addBox(-5.0F, 3.0F, -2.5F, 5.0F, 13.0F, 5.0F)
                        .texOffs(48, 42).addBox(-6.0F, 4.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(-6.5F, -1.0F, 0.0F));

        // 4. Braço Esquerdo maciço com espinho de cristal de gelo
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 62).addBox(-0.5F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                        .texOffs(26, 62).addBox(0.0F, 3.0F, -2.5F, 5.0F, 13.0F, 5.0F)
                        .texOffs(48, 62).addBox(4.0F, 4.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(6.5F, -1.0F, 0.0F));

        // 5. Pernas de blocos de gelo
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 84).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                PartPose.offset(-3.0F, 14.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(22, 84).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                PartPose.offset(3.0F, 14.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(GolemDeGeloEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.1F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.1F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
