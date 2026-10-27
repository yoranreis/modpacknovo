package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.CarapacaBasalticaEntity;
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
public final class CarapacaBasalticaModel extends EntityModel<CarapacaBasalticaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("carapaca_basaltica"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/terras_da_cinza/carapaca_basaltica.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart rearLeftLeg;
    private final ModelPart rearRightLeg;

    public CarapacaBasalticaModel(ModelPart root) {
        super(net.minecraft.client.renderer.RenderType::entityCutoutNoCull);
        this.root = root;
        body = root.getChild("body");
        head = root.getChild("head");
        tail = root.getChild("tail");
        frontLeftLeg = root.getChild("front_left_leg");
        frontRightLeg = root.getChild("front_right_leg");
        rearLeftLeg = root.getChild("rear_left_leg");
        rearRightLeg = root.getChild("rear_right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -3.0F, -7.0F, 10.0F, 7.0F, 15.0F),
                PartPose.offset(0.0F, 15.5F, 0.0F));
        // Layered basalt armor plates run from the shoulders down the back.
        body.addOrReplaceChild("front_plate", CubeListBuilder.create().texOffs(64, 70)
                        .addBox(-4.5F, -5.0F, -6.0F, 9.0F, 5.0F, 5.0F, new CubeDeformation(-0.15F)),
                PartPose.ZERO);
        body.addOrReplaceChild("mid_plate", CubeListBuilder.create().texOffs(64, 70)
                        .addBox(-4.8F, -5.4F, -2.5F, 9.6F, 5.5F, 5.0F, new CubeDeformation(-0.1F)),
                PartPose.ZERO);
        body.addOrReplaceChild("rear_plate", CubeListBuilder.create().texOffs(64, 70)
                        .addBox(-4.4F, -4.8F, 1.5F, 8.8F, 5.0F, 5.0F, new CubeDeformation(-0.1F)),
                PartPose.ZERO);
        body.addOrReplaceChild("ridge_front", CubeListBuilder.create().texOffs(0, 70)
                        .addBox(-2.0F, -7.0F, -5.0F, 4.0F, 3.0F, 3.0F), PartPose.ZERO);
        body.addOrReplaceChild("ridge_mid", CubeListBuilder.create().texOffs(0, 70)
                        .addBox(-1.8F, -6.6F, -1.0F, 3.6F, 3.0F, 3.0F), PartPose.ZERO);
        body.addOrReplaceChild("ridge_rear", CubeListBuilder.create().texOffs(0, 70)
                        .addBox(-1.5F, -6.0F, 3.0F, 3.0F, 2.6F, 3.0F), PartPose.ZERO);

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(20, 0).addBox(-4.0F, -3.5F, -5.0F, 8.0F, 6.0F, 9.0F),
                PartPose.offset(0.0F, 14.0F, -7.0F));
        head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(56, 34)
                        .addBox(-2.8F, -1.5F, -8.0F, 5.6F, 3.5F, 5.0F), PartPose.ZERO);
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(80, 0)
                        .addBox(-2.5F, 1.5F, -7.5F, 5.0F, 1.2F, 5.0F), PartPose.ZERO);
        head.addOrReplaceChild("eye_left", CubeListBuilder.create().texOffs(104, 0)
                        .addBox(-4.1F, -1.6F, -4.1F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("eye_right", CubeListBuilder.create().texOffs(104, 0)
                        .addBox(3.1F, -1.6F, -4.1F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("brow_left", CubeListBuilder.create().texOffs(80, 16)
                        .addBox(-4.0F, -2.5F, -4.8F, 2.0F, 1.5F, 3.0F), PartPose.ZERO);
        head.addOrReplaceChild("brow_right", CubeListBuilder.create().texOffs(80, 16)
                        .addBox(2.0F, -2.5F, -4.8F, 2.0F, 1.5F, 3.0F), PartPose.ZERO);

        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(40, 92)
                        .addBox(-1.8F, -1.5F, 0.0F, 3.6F, 3.0F, 8.0F),
                PartPose.offset(0.0F, 16.0F, 7.0F));

        addLeg(root, "front_left_leg", -3.6F, -5.0F);
        addLeg(root, "front_right_leg", 3.6F, -5.0F);
        addLeg(root, "rear_left_leg", -3.6F, 5.2F);
        addLeg(root, "rear_right_leg", 3.6F, 5.2F);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void addLeg(PartDefinition root, String name, float x, float z) {
        PartDefinition leg = root.addOrReplaceChild(name, CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(x, 17.0F, z));
        leg.addOrReplaceChild("claws", CubeListBuilder.create().texOffs(20, 48)
                        .addBox(-1.7F, 3.6F, -3.0F, 3.4F, 1.4F, 2.0F), PartPose.ZERO);
    }

    @Override
    public void setupAnim(CarapacaBasalticaEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);
        float walk = limbSwing * 0.55F;
        float stride = Math.min(0.85F, limbSwingAmount) * 0.85F;
        frontLeftLeg.xRot = (float) Math.cos(walk) * stride;
        rearRightLeg.xRot = (float) Math.cos(walk) * stride;
        frontRightLeg.xRot = (float) Math.cos(walk + Math.PI) * stride;
        rearLeftLeg.xRot = (float) Math.cos(walk + Math.PI) * stride;
        tail.yRot = (float) Math.sin(ageInTicks * 0.12F) * 0.12F;
        body.y = 15.5F + (float) Math.sin(ageInTicks * 0.09F) * 0.08F;
        if (entity.isAggressive()) {
            head.xRot -= 0.12F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}
