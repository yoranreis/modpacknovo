package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
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
import br.com.caosarcana.common.entity.RastejanteDeCinzaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

@Environment(EnvType.CLIENT)
public final class RastejanteDeCinzaModel extends EntityModel<RastejanteDeCinzaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("rastejante_de_cinza"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/terras_da_cinza/rastejante_de_cinza.png");

    private static final float BODY_REST_Y = 18.5F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart frontLeftLeg;
    private final ModelPart middleLeftLeg;
    private final ModelPart rearLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart middleRightLeg;
    private final ModelPart rearRightLeg;

    public RastejanteDeCinzaModel(ModelPart root) {
        super(net.minecraft.client.renderer.RenderType::entityCutoutNoCull);
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.frontLeftLeg = root.getChild("front_left_leg");
        this.middleLeftLeg = root.getChild("middle_left_leg");
        this.rearLeftLeg = root.getChild("rear_left_leg");
        this.frontRightLeg = root.getChild("front_right_leg");
        this.middleRightLeg = root.getChild("middle_right_leg");
        this.rearRightLeg = root.getChild("rear_right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -2.5F, -3.5F, 10.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, BODY_REST_Y, 0.0F));

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(36, 0)
                        .addBox(-2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 17.0F, -5.1F));

        root.addOrReplaceChild("abdomen",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .addBox(-3.0F, -1.5F, -2.0F, 6.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 21.5F, 5.4F));

        addLeg(root, "front_left_leg", -5.5F, -3.0F);
        addLeg(root, "middle_left_leg", -5.5F, 0.0F);
        addLeg(root, "rear_left_leg", -5.5F, 3.0F);
        addLeg(root, "front_right_leg", 5.5F, -3.0F);
        addLeg(root, "middle_right_leg", 5.5F, 0.0F);
        addLeg(root, "rear_right_leg", 5.5F, 3.0F);

        addMandible(root, "left_mandible", -2.5F);
        addMandible(root, "right_mandible", 2.5F);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addLeg(PartDefinition root, String name, float x, float z) {
        root.addOrReplaceChild(name,
                CubeListBuilder.create()
                        .texOffs(22, 20)
                        .addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(x, 22.5F, z));
    }

    private static void addMandible(PartDefinition root, String name, float x) {
        root.addOrReplaceChild(name,
                CubeListBuilder.create()
                        .texOffs(32, 20)
                        .addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(x, 19.0F, -7.3F));
    }

    @Override
    public void setupAnim(RastejanteDeCinzaEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        body.y = BODY_REST_Y + (float) Math.cos(ageInTicks * 0.18F) * 0.2F;
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float swing = limbSwing * 0.6662F;
        float amplitude = 0.75F * limbSwingAmount;
        frontLeftLeg.xRot = (float) Math.cos(swing) * amplitude;
        middleLeftLeg.xRot = (float) Math.cos(swing + (float) Math.PI) * amplitude;
        rearLeftLeg.xRot = (float) Math.cos(swing) * amplitude;
        frontRightLeg.xRot = (float) Math.cos(swing + (float) Math.PI) * amplitude;
        middleRightLeg.xRot = (float) Math.cos(swing) * amplitude;
        rearRightLeg.xRot = (float) Math.cos(swing + (float) Math.PI) * amplitude;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}

