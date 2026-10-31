package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.VigiaDasBrasasEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class VigiaDasBrasasModel extends EntityModel<VigiaDasBrasasEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("vigia_das_brasas"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/terras_da_cinza/vigia_das_brasas.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart cape;

    public VigiaDasBrasasModel(ModelPart root) {
        super(net.minecraft.client.renderer.RenderType::entityCutoutNoCull);
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
        cape = root.getChild("cape");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        body.addOrReplaceChild("ember_core", CubeListBuilder.create().texOffs(48, 48)
                        .addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, -2.15F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        head.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(0.35F)),
                PartPose.ZERO);

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(-2.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(2.0F, 16.0F, 0.0F));

        PartDefinition cape = root.addOrReplaceChild("cape", CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 12.0F, 1.0F),
                PartPose.offset(0.0F, 7.0F, 2.4F));
        cape.addOrReplaceChild("left_torn_edge", CubeListBuilder.create().texOffs(0, 45)
                        .addBox(-5.0F, 10.0F, 0.0F, 3.0F, 4.0F, 1.0F), PartPose.ZERO);
        cape.addOrReplaceChild("middle_torn_edge", CubeListBuilder.create().texOffs(8, 45)
                        .addBox(-1.0F, 11.0F, 0.0F, 3.0F, 3.0F, 1.0F), PartPose.ZERO);
        cape.addOrReplaceChild("right_torn_edge", CubeListBuilder.create().texOffs(16, 45)
                        .addBox(3.0F, 9.0F, 0.0F, 2.0F, 5.0F, 1.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(VigiaDasBrasasEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);
        float walk = limbSwing * 0.6662F;
        rightLeg.xRot = (float) Math.cos(walk) * 0.75F * limbSwingAmount;
        leftLeg.xRot = (float) Math.cos(walk + Math.PI) * 0.75F * limbSwingAmount;
        rightArm.xRot = -0.25F + (float) Math.cos(walk + Math.PI) * 0.35F * limbSwingAmount;
        leftArm.xRot = -0.25F + (float) Math.cos(walk) * 0.35F * limbSwingAmount;
        if (entity.isAggressive()) {
            rightArm.xRot = -1.5F + (float) Math.sin(ageInTicks * 0.16F) * 0.08F;
            leftArm.xRot = -1.5F - (float) Math.sin(ageInTicks * 0.16F) * 0.08F;
        }
        cape.xRot = 0.08F + limbSwingAmount * 0.12F + (float) Math.sin(ageInTicks * 0.08F) * 0.025F;
        body.y = 12.0F + (float) Math.sin(ageInTicks * 0.08F) * 0.08F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}
