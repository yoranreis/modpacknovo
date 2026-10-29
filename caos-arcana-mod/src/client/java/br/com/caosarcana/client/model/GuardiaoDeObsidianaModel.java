package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.GuardiaoDeObsidianaEntity;
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
public final class GuardiaoDeObsidianaModel extends EntityModel<GuardiaoDeObsidianaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("guardiao_de_obsidiana"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/recife_coral_abissal/guardiao_de_obsidiana.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GuardiaoDeObsidianaModel(ModelPart root) {
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

        // 1. Elmo de obsidiana com visor em T incandescente e crista vulcânica
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(36, 0).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        // 2. Torso pesado de obsidiana com núcleo de magma exposto e cristais dorsais
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-5.5F, 0.0F, -3.0F, 11.0F, 12.0F, 6.0F)
                        .texOffs(38, 20).addBox(-4.0F, 12.0F, -2.5F, 8.0F, 4.0F, 5.0F)
                        .texOffs(68, 20).addBox(-4.5F, -3.0F, 2.0F, 3.0F, 8.0F, 3.0F)
                        .texOffs(84, 20).addBox(1.5F, -3.0F, 2.0F, 3.0F, 8.0F, 3.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        // 3. Braço Direito com ombreira e espinho de obsidiana
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-4.5F, -3.0F, -3.0F, 5.0F, 5.0F, 6.0F)
                        .texOffs(26, 42).addBox(-3.5F, -9.0F, -1.0F, 2.0F, 6.0F, 2.0F)
                        .texOffs(38, 42).addBox(-4.0F, 2.0F, -2.5F, 4.0F, 12.0F, 5.0F),
                PartPose.offset(-6.0F, 0.0F, 0.0F));

        // 4. Braço Esquerdo com ombreira e espinho de obsidiana
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 62).addBox(-0.5F, -3.0F, -3.0F, 5.0F, 5.0F, 6.0F)
                        .texOffs(26, 62).addBox(1.5F, -9.0F, -1.0F, 2.0F, 6.0F, 2.0F)
                        .texOffs(38, 62).addBox(0.0F, 2.0F, -2.5F, 4.0F, 12.0F, 5.0F),
                PartPose.offset(6.0F, 0.0F, 0.0F));

        // 5. Pernas de rocha vulcânica
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 82).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 10.0F, 5.0F),
                PartPose.offset(-2.8F, 14.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(22, 82).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 10.0F, 5.0F),
                PartPose.offset(2.8F, 14.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(GuardiaoDeObsidianaEntity entity, float limbSwing, float limbSwingAmount,
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
