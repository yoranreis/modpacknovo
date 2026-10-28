package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.GuardiaoMadeiraNegraEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class GuardiaoMadeiraNegraModel extends EntityModel<GuardiaoMadeiraNegraEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("guardiao_madeira_negra"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/floresta_negra/guardiao_madeira_negra.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GuardiaoMadeiraNegraModel(ModelPart root) {
        super(net.minecraft.client.renderer.RenderType::entityCutoutNoCull);
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -5.0F, -3.0F, 8.0F, 11.0F, 6.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        body.addOrReplaceChild("bark_ridge", CubeListBuilder.create().texOffs(32, 32)
                        .addBox(-4.4F, -3.0F, -3.3F, 2.0F, 8.0F, 1.0F), PartPose.ZERO);
        body.addOrReplaceChild("moss_patch", CubeListBuilder.create().texOffs(80, 0)
                        .addBox(1.2F, -4.0F, -3.35F, 2.6F, 3.0F, 0.7F), PartPose.ZERO);

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.0F, -4.0F, -3.5F, 8.0F, 8.0F, 7.0F),
                PartPose.offset(0.0F, 5.0F, 0.0F));
        head.addOrReplaceChild("face_plate", CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-3.0F, -2.0F, -4.1F, 6.0F, 4.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(24, 48)
                        .addBox(-2.8F, -1.2F, -4.2F, 1.3F, 1.2F, 0.5F), PartPose.ZERO);
        head.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(24, 48)
                        .addBox(1.5F, -1.2F, -4.2F, 1.3F, 1.2F, 0.5F), PartPose.ZERO);
        head.addOrReplaceChild("left_branch", CubeListBuilder.create().texOffs(48, 32)
                        .addBox(-7.0F, -6.0F, -1.0F, 4.0F, 3.0F, 3.0F), PartPose.ZERO);
        head.addOrReplaceChild("left_twig", CubeListBuilder.create().texOffs(64, 32)
                        .addBox(-7.0F, -8.0F, -0.5F, 2.0F, 3.0F, 2.0F), PartPose.ZERO);
        head.addOrReplaceChild("right_branch", CubeListBuilder.create().texOffs(48, 32)
                        .addBox(3.0F, -6.0F, -1.0F, 4.0F, 3.0F, 3.0F), PartPose.ZERO);
        head.addOrReplaceChild("right_twig", CubeListBuilder.create().texOffs(64, 32)
                        .addBox(5.0F, -8.0F, -0.5F, 2.0F, 3.0F, 2.0F), PartPose.ZERO);

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 10.0F, 4.0F),
                PartPose.offset(-5.0F, 9.0F, 0.0F));
        rightArm.addOrReplaceChild("right_claw", CubeListBuilder.create().texOffs(32, 48)
                        .addBox(-3.0F, 6.0F, -2.5F, 2.0F, 4.0F, 2.0F), PartPose.ZERO);
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(16, 32).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 10.0F, 4.0F),
                PartPose.offset(5.0F, 9.0F, 0.0F));
        leftArm.addOrReplaceChild("left_claw", CubeListBuilder.create().texOffs(32, 48)
                        .addBox(1.0F, 6.0F, -2.5F, 2.0F, 4.0F, 2.0F), PartPose.ZERO);

        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 68)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(-2.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 68)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(2.0F, 16.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(GuardiaoMadeiraNegraEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);
        float walk = limbSwing * 0.62F;
        float stride = limbSwingAmount * 0.7F;
        rightLeg.xRot = (float) Math.cos(walk) * stride;
        leftLeg.xRot = (float) Math.cos(walk + Math.PI) * stride;
        rightArm.xRot = (float) Math.cos(walk + Math.PI) * stride - 0.08F;
        leftArm.xRot = (float) Math.cos(walk) * stride - 0.08F;
        if (entity.isAggressive()) {
            rightArm.xRot = -1.1F + (float) Math.sin(ageInTicks * 0.22F) * 0.12F;
            leftArm.xRot = -1.1F - (float) Math.sin(ageInTicks * 0.22F) * 0.12F;
        }
        body.y = 12.0F + (float) Math.sin(ageInTicks * 0.07F) * 0.08F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}
