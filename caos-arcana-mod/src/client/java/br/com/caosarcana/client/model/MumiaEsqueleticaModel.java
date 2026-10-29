package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.MumiaEsqueleticaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmedModel;
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
import net.minecraft.world.entity.HumanoidArm;

@Environment(EnvType.CLIENT)
public final class MumiaEsqueleticaModel extends EntityModel<MumiaEsqueleticaEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("mumia_esqueletica"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/deserto_dunas_douradas/mumia_esqueletica.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public MumiaEsqueleticaModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
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

        // Crânio enfaixado com olhos vermelhos e camada externa de bandagens
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-4.3F, -8.3F, -4.3F, 9.0F, 9.0F, 9.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Torso esquelético com ombreiras faraônicas azul/ouro e faixas penduradas
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-5.0F, 0.0F, -2.5F, 10.0F, 4.0F, 5.0F)
                        .texOffs(30, 20).addBox(-8.5F, -0.5F, -2.5F, 4.0F, 3.0F, 5.0F)
                        .texOffs(48, 20).addBox(4.5F, -0.5F, -2.5F, 4.0F, 3.0F, 5.0F)
                        .texOffs(0, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 10.0F, 4.0F)
                        .texOffs(24, 32).addBox(-4.0F, 9.0F, -2.0F, 8.0F, 3.0F, 4.0F)
                        .texOffs(48, 32).addBox(-3.0F, 11.5F, -2.1F, 6.0F, 6.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Braços e pernas esqueléticas enfaixadas
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-2.0F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(-5.5F, 2.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(12, 48).addBox(-1.0F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(5.5F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(24, 48).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(36, 48).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(MumiaEsqueleticaEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float walk = limbSwing * 0.66F;
        float stride = limbSwingAmount * 0.75F;
        rightLeg.xRot = Mth.cos(walk) * stride;
        leftLeg.xRot = Mth.cos(walk + (float) Math.PI) * stride;
        leftArm.xRot = Mth.cos(walk) * stride * 0.7F;

        if (entity.isInDesertFury()) {
            rightArm.xRot = -1.1F + Mth.sin(ageInTicks * 0.35F) * 0.18F;
            leftArm.xRot = -0.65F - Mth.sin(ageInTicks * 0.35F) * 0.12F;
        } else {
            rightArm.xRot = -0.45F + Mth.cos(walk + (float) Math.PI) * stride * 0.65F;
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        ModelPart part = arm == HumanoidArm.RIGHT ? rightArm : leftArm;
        part.translateAndRotate(poseStack);
        poseStack.translate(arm == HumanoidArm.RIGHT ? -0.04F : 0.04F, 0.12F, -0.05F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
