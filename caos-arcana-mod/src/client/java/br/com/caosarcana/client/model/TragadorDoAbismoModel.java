package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.TragadorDoAbismoEntity;
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
public final class TragadorDoAbismoModel extends EntityModel<TragadorDoAbismoEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("tragador_do_abismo"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/recife_coral_abissal/tragador_do_abismo.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart tentacleBase;
    private final ModelPart tentacleFL;
    private final ModelPart tentacleFR;
    private final ModelPart tentacleBL;
    private final ModelPart tentacleBR;

    public TragadorDoAbismoModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.tentacleBase = root.getChild("tentacle_base");
        this.tentacleFL = this.tentacleBase.getChild("tentacle_fl");
        this.tentacleFR = this.tentacleBase.getChild("tentacle_fr");
        this.tentacleBL = this.tentacleBase.getChild("tentacle_bl");
        this.tentacleBR = this.tentacleBase.getChild("tentacle_br");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Cabeça sem olhos com bocarra circular de lampreia e cracas no topo
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(36, 0).addBox(-3.0F, -7.0F, -5.5F, 6.0F, 6.0F, 2.0F)
                        .texOffs(56, 0).addBox(-2.0F, -10.0F, -2.5F, 4.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, 2.0F, -0.5F));

        // 2. Torso com costelas abertas, núcleo bioluminescente ciano e filamentos dorsais
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 11.0F, 5.0F)
                        .texOffs(30, 20).addBox(-2.0F, 2.0F, -1.5F, 4.0F, 6.0F, 3.0F)
                        .texOffs(48, 20).addBox(-2.0F, 1.0F, 2.5F, 4.0F, 9.0F, 3.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));

        // 3. Braço Direito com cracas/corais no ombro e garras abissais
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-4.0F, -2.5F, -2.5F, 5.0F, 4.0F, 5.0F)
                        .texOffs(24, 40).addBox(-3.0F, 1.5F, -2.0F, 3.0F, 10.0F, 4.0F)
                        .texOffs(42, 40).addBox(-3.0F, 10.5F, -3.5F, 3.0F, 4.0F, 5.0F),
                PartPose.offset(-5.0F, 3.5F, 0.0F));

        // 4. Braço Esquerdo com cracas/corais no ombro e garras abissais
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 58).addBox(-1.0F, -2.5F, -2.5F, 5.0F, 4.0F, 5.0F)
                        .texOffs(24, 58).addBox(0.0F, 1.5F, -2.0F, 3.0F, 10.0F, 4.0F)
                        .texOffs(42, 58).addBox(0.0F, 10.5F, -3.5F, 3.0F, 4.0F, 5.0F),
                PartPose.offset(5.0F, 3.5F, 0.0F));

        // 5. Base de tentáculos inferiores no lugar das pernas
        PartDefinition tentacleBase = root.addOrReplaceChild("tentacle_base",
                CubeListBuilder.create()
                        .texOffs(0, 76).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));

        tentacleBase.addOrReplaceChild("tentacle_fl",
                CubeListBuilder.create()
                        .texOffs(26, 76).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offset(-2.0F, 3.0F, -1.0F));

        tentacleBase.addOrReplaceChild("tentacle_fr",
                CubeListBuilder.create()
                        .texOffs(36, 76).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offset(2.0F, 3.0F, -1.0F));

        tentacleBase.addOrReplaceChild("tentacle_bl",
                CubeListBuilder.create()
                        .texOffs(46, 76).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offset(-2.0F, 3.0F, 1.0F));

        tentacleBase.addOrReplaceChild("tentacle_br",
                CubeListBuilder.create()
                        .texOffs(56, 76).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offset(2.0F, 3.0F, 1.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(TragadorDoAbismoEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        if (entity.isSuctionActive()) {
            this.rightArm.xRot = -0.95F + Mth.sin(ageInTicks * 0.35F) * 0.15F;
            this.leftArm.xRot = -0.95F - Mth.sin(ageInTicks * 0.35F) * 0.15F;
            this.rightArm.zRot = 0.25F;
            this.leftArm.zRot = -0.25F;
        } else {
            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount - 0.2F;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount - 0.2F;
            this.rightArm.zRot = 0.1F;
            this.leftArm.zRot = -0.1F;
        }

        float wave = Mth.sin(ageInTicks * 0.16F) * 0.22F;
        float walk = Mth.cos(limbSwing * 0.6662F) * 0.7F * limbSwingAmount;
        this.tentacleFL.xRot = walk + wave;
        this.tentacleFR.xRot = -walk - wave;
        this.tentacleBL.xRot = -walk + wave + 0.15F;
        this.tentacleBR.xRot = walk - wave + 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
