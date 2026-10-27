package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.CaoFumeganteEntity;
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

@Environment(EnvType.CLIENT)
public final class CaoFumeganteModel extends EntityModel<CaoFumeganteEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("cao_fumegante"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/terras_da_cinza/cao_fumegante.png");

    private static final float BODY_REST_Y = 16.5F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart rearLeftLeg;
    private final ModelPart rearRightLeg;
    private final ModelPart tail;

    public CaoFumeganteModel(ModelPart root) {
        super(net.minecraft.client.renderer.RenderType::entityCutoutNoCull);
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.frontLeftLeg = root.getChild("front_left_leg");
        this.frontRightLeg = root.getChild("front_right_leg");
        this.rearLeftLeg = root.getChild("rear_left_leg");
        this.rearRightLeg = root.getChild("rear_right_leg");
        this.tail = root.getChild("tail");

        // Silhueta mais alta e esguia sem alterar o atlas UV aprovado.
        this.body.xScale = 0.72F;
        this.body.yScale = 1.2F;
        this.head.xScale = 0.9F;
        this.head.yScale = 1.1F;
        this.frontLeftLeg.xScale = 0.72F;
        this.frontRightLeg.xScale = 0.72F;
        this.rearLeftLeg.xScale = 0.72F;
        this.rearRightLeg.xScale = 0.72F;
        this.frontLeftLeg.zScale = 0.78F;
        this.frontRightLeg.zScale = 0.78F;
        this.rearLeftLeg.zScale = 0.78F;
        this.rearRightLeg.zScale = 0.78F;
        this.frontLeftLeg.yScale = 1.45F;
        this.frontRightLeg.yScale = 1.45F;
        this.rearLeftLeg.yScale = 1.45F;
        this.rearRightLeg.yScale = 1.45F;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -3.0F, -5.5F, 8.0F, 6.0F, 11.0F),
                PartPose.offset(0.0F, BODY_REST_Y, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(40, 0)
                        .addBox(-3.0F, -2.5F, -2.5F, 6.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 14.2F, -6.5F));

        PartDefinition muzzle = head.addOrReplaceChild("muzzle",
                CubeListBuilder.create()
                        .texOffs(40, 12)
                        .addBox(-2.0F, -1.5F, -2.0F, 4.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 1.5F, -2.3F));

        muzzle.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(46, 22)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -1.6F));

        head.addOrReplaceChild("left_ear",
                CubeListBuilder.create()
                        .texOffs(28, 22)
                        .addBox(-1.0F, -1.5F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(-2.2F, -1.8F, -0.3F));
        head.addOrReplaceChild("right_ear",
                CubeListBuilder.create()
                        .texOffs(36, 22)
                        .addBox(-1.0F, -1.5F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(2.2F, -1.8F, -0.3F));

        addLeg(root, "front_left_leg", -2.4F, -3.5F);
        addLeg(root, "front_right_leg", 2.4F, -3.5F);
        addLeg(root, "rear_left_leg", -2.4F, 4.0F);
        addLeg(root, "rear_right_leg", 2.4F, 4.0F);

        root.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(14, 22)
                        .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 15.5F, 5.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addLeg(PartDefinition root, String name, float x, float z) {
        root.addOrReplaceChild(name,
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(x, 17.0F, z));
    }

    @Override
    public void setupAnim(CaoFumeganteEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        body.y = BODY_REST_Y + (float) Math.cos(ageInTicks * 0.16F) * 0.12F;
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float swing = limbSwing * 0.6662F;
        float amplitude = 0.9F * limbSwingAmount;
        frontLeftLeg.xRot = (float) Math.cos(swing) * amplitude;
        rearRightLeg.xRot = (float) Math.cos(swing) * amplitude;
        frontRightLeg.xRot = (float) Math.cos(swing + (float) Math.PI) * amplitude;
        rearLeftLeg.xRot = (float) Math.cos(swing + (float) Math.PI) * amplitude;
        tail.yRot = (float) Math.sin(ageInTicks * 0.2F) * 0.12F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}
