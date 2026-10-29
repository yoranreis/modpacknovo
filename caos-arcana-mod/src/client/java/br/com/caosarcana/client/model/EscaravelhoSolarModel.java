package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.EscaravelhoSolarEntity;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class EscaravelhoSolarModel extends EntityModel<EscaravelhoSolarEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("escaravelho_solar"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/deserto_dunas_douradas/escaravelho_solar.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart thorax;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightMidLeg;
    private final ModelPart leftMidLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;

    public EscaravelhoSolarModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.head = root.getChild("head");
        this.thorax = root.getChild("thorax");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.rightMidLeg = root.getChild("right_mid_leg");
        this.leftMidLeg = root.getChild("left_mid_leg");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cabeça blindada com olhos compostos solares, pinças e chifres duplos curvados
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -2.5F, -4.5F, 7.0F, 5.0F, 5.0F)
                        .texOffs(26, 0).addBox(-1.5F, -0.5F, -5.8F, 3.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 18.0F, -6.0F));

        head.addOrReplaceChild("right_mandible", CubeListBuilder.create()
                        .texOffs(38, 0).addBox(-3.0F, 1.5F, -6.8F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.15F, -0.25F, 0.0F));
        head.addOrReplaceChild("left_mandible", CubeListBuilder.create()
                        .texOffs(50, 0).addBox(1.0F, 1.5F, -6.8F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.15F, 0.25F, 0.0F));

        head.addOrReplaceChild("right_horn_base", CubeListBuilder.create()
                        .texOffs(62, 0).addBox(-5.2F, -5.5F, -4.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.28F, 0.0F, -0.25F));
        head.addOrReplaceChild("right_horn_tip", CubeListBuilder.create()
                        .texOffs(72, 0).addBox(-6.0F, -9.0F, -5.2F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.55F, 0.0F, 0.12F));

        head.addOrReplaceChild("left_horn_base", CubeListBuilder.create()
                        .texOffs(82, 0).addBox(3.2F, -5.5F, -4.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.28F, 0.0F, 0.25F));
        head.addOrReplaceChild("left_horn_tip", CubeListBuilder.create()
                        .texOffs(92, 0).addBox(4.0F, -9.0F, -5.2F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.55F, 0.0F, -0.12F));

        // Tórax (Pronoto) e Carapaça Solar Abaulada (Élitros)
        PartDefinition thorax = root.addOrReplaceChild("thorax", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-5.0F, -4.5F, -6.0F, 10.0F, 7.0F, 5.0F)
                        .texOffs(32, 12).addBox(-4.0F, -5.5F, -5.5F, 8.0F, 1.0F, 4.0F)
                        .texOffs(0, 26).addBox(-6.0F, -5.0F, -1.0F, 12.0F, 8.0F, 11.0F)
                        .texOffs(48, 26).addBox(-5.0F, -6.5F, -0.5F, 10.0F, 2.0F, 9.0F)
                        .texOffs(0, 46).addBox(-5.0F, -4.0F, 10.0F, 10.0F, 6.0F, 2.0F)
                        .texOffs(26, 46).addBox(-6.6F, -2.5F, -0.5F, 1.0F, 4.0F, 10.0F)
                        .texOffs(50, 46).addBox(5.6F, -2.5F, -0.5F, 1.0F, 4.0F, 10.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        // 6 Patas Blindadas Articuladas
        addArmoredLeg(root, "right_front_leg", -5.2F, 19.5F, -4.0F, true, 0.25F);
        addArmoredLeg(root, "left_front_leg", 5.2F, 19.5F, -4.0F, false, 0.25F);
        addArmoredLeg(root, "right_mid_leg", -5.8F, 19.5F, 1.0F, true, 0.0F);
        addArmoredLeg(root, "left_mid_leg", 5.8F, 19.5F, 1.0F, false, 0.0F);
        addArmoredLeg(root, "right_hind_leg", -5.5F, 19.5F, 6.0F, true, -0.25F);
        addArmoredLeg(root, "left_hind_leg", 5.5F, 19.5F, 6.0F, false, -0.25F);

        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void addArmoredLeg(PartDefinition root, String name, float x, float y, float z,
                                      boolean isRight, float yawOffset) {
        int uFemur = isRight ? 0 : 24;
        int uTibia = isRight ? 14 : 38;
        float dir = isRight ? -1.0F : 1.0F;

        PartDefinition leg = root.addOrReplaceChild(name, CubeListBuilder.create()
                        .texOffs(uFemur, 62).addBox(isRight ? -3.0F : 0.0F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(x, y, z, 0.0F, dir * yawOffset, dir * -0.2F));

        leg.addOrReplaceChild("tibia", CubeListBuilder.create()
                        .texOffs(uTibia, 62).addBox(isRight ? -3.8F : 1.8F, -0.5F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, dir * 0.25F));
    }

    @Override
    public void setupAnim(EscaravelhoSolarEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F) * 0.65F;
        head.xRot = headPitch * ((float) Math.PI / 180.0F) * 0.5F;

        if (entity.isChargingSolar()) {
            // Abaixa a cabeça apontando os chifres dourados antes da investida
            head.xRot = 0.45F + Mth.sin(ageInTicks * 0.65F) * 0.08F;
            thorax.y = 18.4F + Mth.sin(ageInTicks * 0.8F) * 0.15F;
        } else {
            thorax.y = 18.0F + Mth.sin(ageInTicks * 0.1F) * 0.08F;
        }

        float speed = limbSwing * 0.85F;
        float swing = limbSwingAmount * 0.65F;

        rightFrontLeg.yRot = -0.25F + Mth.cos(speed) * swing;
        leftFrontLeg.yRot = 0.25F - Mth.cos(speed + (float) Math.PI) * swing;

        rightMidLeg.yRot = Mth.cos(speed + (float) Math.PI) * swing;
        leftMidLeg.yRot = -Mth.cos(speed) * swing;

        rightHindLeg.yRot = 0.25F + Mth.cos(speed) * swing;
        leftHindLeg.yRot = -0.25F - Mth.cos(speed + (float) Math.PI) * swing;

        rightFrontLeg.zRot = 0.2F + Math.abs(Mth.sin(speed)) * swing * 0.45F;
        leftFrontLeg.zRot = -0.2F - Math.abs(Mth.sin(speed + (float) Math.PI)) * swing * 0.45F;
        rightMidLeg.zRot = 0.2F + Math.abs(Mth.sin(speed + (float) Math.PI)) * swing * 0.45F;
        leftMidLeg.zRot = -0.2F - Math.abs(Mth.sin(speed)) * swing * 0.45F;
        rightHindLeg.zRot = 0.2F + Math.abs(Mth.sin(speed)) * swing * 0.45F;
        leftHindLeg.zRot = -0.2F - Math.abs(Mth.sin(speed + (float) Math.PI)) * swing * 0.45F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
