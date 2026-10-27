package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.MineiroOcoEntity;
import net.minecraft.client.model.ArmedModel;
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
import net.minecraft.world.entity.HumanoidArm;

@Environment(EnvType.CLIENT)
public final class MineiroOcoModel extends EntityModel<MineiroOcoEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("mineiro_oco"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/terras_da_cinza/mineiro_oco.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public MineiroOcoModel(ModelPart root) {
        super(net.minecraft.client.renderer.RenderType::entityCutoutNoCull);
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

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -6.0F, -2.0F, 8.0F, 10.0F, 4.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 7.0F, 6.0F),
                PartPose.offset(0.0F, 5.0F, 0.0F));
        head.addOrReplaceChild("helmet",
                CubeListBuilder.create().texOffs(32, 20)
                        .addBox(-4.0F, -5.0F, -4.0F, 8.0F, 3.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet_lamp",
                CubeListBuilder.create().texOffs(28, 40)
                        .addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -3.5F, -4.0F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offset(-5.5F, 10.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(14, 20)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offset(5.5F, 10.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 38)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(-2.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(14, 38)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(2.0F, 17.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        (arm == HumanoidArm.RIGHT ? rightArm : leftArm).translateAndRotate(poseStack);
    }

    @Override
    public void setupAnim(MineiroOcoEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float walk = limbSwing * 0.6662F;
        float step = 0.65F * limbSwingAmount;
        rightLeg.xRot = (float) Math.cos(walk) * step;
        leftLeg.xRot = (float) Math.cos(walk + Math.PI) * step;

        rightArm.xRot = (float) Math.cos(walk + Math.PI) * 0.5F * limbSwingAmount;
        leftArm.xRot = (float) Math.cos(walk) * 0.5F * limbSwingAmount;
        if (entity.isAggressive()) {
            rightArm.xRot = -0.9F + (float) Math.sin(ageInTicks * 0.18F) * 0.06F;
            leftArm.xRot = -0.12F;
        }
        if (this.attackTime > 0.0F) {
            float strike = (float) Math.sin(this.attackTime * Math.PI);
            rightArm.xRot = -1.2F + strike * 1.8F;
            rightArm.zRot = -0.12F - strike * 0.18F;
        } else {
            rightArm.zRot = -0.08F;
        }
        leftArm.zRot = 0.08F;
        body.y = 12.0F + (float) Math.cos(ageInTicks * 0.08F) * 0.08F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}
