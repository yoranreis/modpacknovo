package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.AranhaCascaSoltaEntity;
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
public final class AranhaCascaSoltaModel extends EntityModel<AranhaCascaSoltaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("aranha_casca_solta"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/floresta_negra/aranha_casca_solta.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body0;
    private final ModelPart body1;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleHindLeg;
    private final ModelPart leftMiddleHindLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public AranhaCascaSoltaModel(ModelPart root) {
        super(net.minecraft.client.renderer.RenderType::entityCutoutNoCull);
        this.root = root;
        this.head = root.getChild("head");
        this.body0 = root.getChild("body0");
        this.body1 = root.getChild("body1");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightMiddleHindLeg = root.getChild("right_middle_hind_leg");
        this.leftMiddleHindLeg = root.getChild("left_middle_hind_leg");
        this.rightMiddleFrontLeg = root.getChild("right_middle_front_leg");
        this.leftMiddleFrontLeg = root.getChild("left_middle_front_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Cabeça com mandíbulas farpadas
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.0F, -3.0F, -6.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 15.0F, -3.0F));

        head.addOrReplaceChild("left_fang",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-2.5F, 1.0F, -7.5F, 2.0F, 4.0F, 2.0F),
                PartPose.ZERO);

        head.addOrReplaceChild("right_fang",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(0.5F, 1.0F, -7.5F, 2.0F, 4.0F, 2.0F),
                PartPose.ZERO);

        // 2. Tórax com casca
        PartDefinition body0 = root.addOrReplaceChild("body0",
                CubeListBuilder.create().texOffs(0, 14)
                        .addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        body0.addOrReplaceChild("thorax_bark",
                CubeListBuilder.create().texOffs(26, 14)
                        .addBox(-3.5F, -3.2F, -2.5F, 7.0F, 2.0F, 5.0F),
                PartPose.ZERO);

        // 3. Abdômen volumoso com placas de casca solta e líquen
        PartDefinition body1 = root.addOrReplaceChild("body1",
                CubeListBuilder.create().texOffs(0, 26)
                        .addBox(-4.5F, -3.5F, 0.0F, 9.0F, 7.0F, 10.0F),
                PartPose.offset(0.0F, 14.5F, 3.0F));

        body1.addOrReplaceChild("bark_top",
                CubeListBuilder.create().texOffs(40, 26)
                        .addBox(-4.0F, -4.5F, 1.0F, 8.0F, 2.0F, 8.0F),
                PartPose.ZERO);

        body1.addOrReplaceChild("bark_left_flare",
                CubeListBuilder.create().texOffs(74, 26)
                        .addBox(-5.2F, -2.5F, 2.0F, 1.0F, 4.0F, 6.0F),
                PartPose.ZERO);

        body1.addOrReplaceChild("bark_right_flare",
                CubeListBuilder.create().texOffs(90, 26)
                        .addBox(4.2F, -2.5F, 2.0F, 1.0F, 4.0F, 6.0F),
                PartPose.ZERO);

        body1.addOrReplaceChild("moss_spinneret",
                CubeListBuilder.create().texOffs(0, 44)
                        .addBox(-2.5F, -1.5F, 9.5F, 5.0F, 3.0F, 2.0F),
                PartPose.ZERO);

        // 4. Patas articuladas
        root.addOrReplaceChild("right_hind_leg",
                CubeListBuilder.create().texOffs(0, 50)
                        .addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                PartPose.offset(-4.0F, 15.0F, 2.0F));

        root.addOrReplaceChild("left_hind_leg",
                CubeListBuilder.create().texOffs(38, 50)
                        .addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                PartPose.offset(4.0F, 15.0F, 2.0F));

        root.addOrReplaceChild("right_middle_hind_leg",
                CubeListBuilder.create().texOffs(0, 56)
                        .addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                PartPose.offset(-4.0F, 15.0F, 1.0F));

        root.addOrReplaceChild("left_middle_hind_leg",
                CubeListBuilder.create().texOffs(38, 56)
                        .addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                PartPose.offset(4.0F, 15.0F, 1.0F));

        root.addOrReplaceChild("right_middle_front_leg",
                CubeListBuilder.create().texOffs(0, 62)
                        .addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                PartPose.offset(-4.0F, 15.0F, 0.0F));

        root.addOrReplaceChild("left_middle_front_leg",
                CubeListBuilder.create().texOffs(38, 62)
                        .addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                PartPose.offset(4.0F, 15.0F, 0.0F));

        root.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create().texOffs(0, 68)
                        .addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                PartPose.offset(-4.0F, 15.0F, -1.0F));

        root.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create().texOffs(38, 68)
                        .addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                PartPose.offset(4.0F, 15.0F, -1.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(AranhaCascaSoltaEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float f = -(Mth.cos(limbSwing * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbSwingAmount;
        float f1 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * limbSwingAmount;
        float f2 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + ((float) Math.PI / 2.0F)) * 0.4F) * limbSwingAmount;
        float f3 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + ((float) Math.PI * 1.5F)) * 0.4F) * limbSwingAmount;

        float f4 = Math.abs(Mth.sin(limbSwing * 0.6662F + 0.0F) * 0.4F) * limbSwingAmount;
        float f5 = Math.abs(Mth.sin(limbSwing * 0.6662F + (float) Math.PI) * 0.4F) * limbSwingAmount;
        float f6 = Math.abs(Mth.sin(limbSwing * 0.6662F + ((float) Math.PI / 2.0F)) * 0.4F) * limbSwingAmount;
        float f7 = Math.abs(Mth.sin(limbSwing * 0.6662F + ((float) Math.PI * 1.5F)) * 0.4F) * limbSwingAmount;

        float zRotBase = 0.7853982F;
        rightHindLeg.zRot = -zRotBase + f4;
        leftHindLeg.zRot = zRotBase - f4;
        rightMiddleHindLeg.zRot = -zRotBase * 0.74F + f5;
        leftMiddleHindLeg.zRot = zRotBase * 0.74F - f5;
        rightMiddleFrontLeg.zRot = -zRotBase * 0.74F + f6;
        leftMiddleFrontLeg.zRot = zRotBase * 0.74F - f6;
        rightFrontLeg.zRot = -zRotBase + f7;
        leftFrontLeg.zRot = zRotBase - f7;

        float yRotBase = 0.7853982F;
        rightHindLeg.yRot = -yRotBase + f;
        leftHindLeg.yRot = yRotBase - f;
        rightMiddleHindLeg.yRot = -yRotBase * 0.33F + f1;
        leftMiddleHindLeg.yRot = yRotBase * 0.33F - f1;
        rightMiddleFrontLeg.yRot = yRotBase * 0.33F + f2;
        leftMiddleFrontLeg.yRot = -yRotBase * 0.33F - f2;
        rightFrontLeg.yRot = yRotBase + f3;
        leftFrontLeg.yRot = -yRotBase - f3;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
