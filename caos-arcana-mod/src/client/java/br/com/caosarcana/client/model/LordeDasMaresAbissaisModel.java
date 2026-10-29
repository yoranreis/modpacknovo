package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.LordeDasMaresAbissaisEntity;
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
public final class LordeDasMaresAbissaisModel extends EntityModel<LordeDasMaresAbissaisEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("lorde_das_mares_abissais"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/recife_coral_abissal/lorde_das_mares_abissais.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart torso;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart tailUpper;
    private final ModelPart tailMid;
    private final ModelPart tailFin;

    public LordeDasMaresAbissaisModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.torso = root.getChild("torso");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.tailUpper = root.getChild("tail_upper");
        this.tailMid = this.tailUpper.getChild("tail_mid");
        this.tailFin = this.tailMid.getChild("tail_fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Cabeça real abissal com Coroa Dourada e Corais
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(36, 0).addBox(-5.0F, -12.0F, -5.0F, 10.0F, 5.0F, 10.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        // 2. Peitoral real com cristal losangular ciano-magenta e manto de tentáculos dorsais
        root.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-5.5F, 0.0F, -3.0F, 11.0F, 12.0F, 6.0F)
                        .texOffs(38, 20).addBox(-4.0F, 12.0F, -2.5F, 8.0F, 5.0F, 5.0F)
                        .texOffs(68, 20).addBox(-5.0F, 0.0F, 3.0F, 10.0F, 14.0F, 3.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        // 3. Braço Direito empunhando o Tridente Dourado Abissal
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-4.5F, -3.0F, -3.0F, 5.0F, 5.0F, 6.0F)
                        .texOffs(26, 42).addBox(-4.0F, 2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-6.0F, -6.0F, 0.0F));

        rightArm.addOrReplaceChild("abyssal_trident",
                CubeListBuilder.create()
                        .texOffs(98, 0).addBox(-2.5F, -14.0F, -4.0F, 1.0F, 28.0F, 1.0F)
                        .texOffs(104, 0).addBox(-5.5F, -15.0F, -4.5F, 7.0F, 2.0F, 2.0F)
                        .texOffs(104, 6).addBox(-5.5F, -22.0F, -4.0F, 1.0F, 7.0F, 1.0F)
                        .texOffs(104, 6).addBox(-2.5F, -23.0F, -4.0F, 1.0F, 8.0F, 1.0F)
                        .texOffs(104, 6).addBox(0.5F, -22.0F, -4.0F, 1.0F, 7.0F, 1.0F)
                        .texOffs(104, 16).addBox(-3.5F, -19.5F, -5.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        // 4. Braço Esquerdo com ombreira de cracas e corais
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 60).addBox(-0.5F, -3.0F, -3.0F, 5.0F, 5.0F, 6.0F)
                        .texOffs(26, 60).addBox(0.0F, 2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(6.0F, -6.0F, 0.0F));

        // 5. Cauda Serpentina Real Abissal
        PartDefinition tailUpper = root.addOrReplaceChild("tail_upper",
                CubeListBuilder.create()
                        .texOffs(0, 78).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 8.0F, 6.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition tailMid = tailUpper.addOrReplaceChild("tail_mid",
                CubeListBuilder.create()
                        .texOffs(30, 78).addBox(-3.0F, 0.0F, -2.5F, 6.0F, 7.0F, 5.0F),
                PartPose.offset(0.0F, 7.5F, 0.5F));

        tailMid.addOrReplaceChild("tail_fin",
                CubeListBuilder.create()
                        .texOffs(54, 78).addBox(-6.0F, 0.0F, -1.0F, 12.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, 6.5F, 1.5F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(LordeDasMaresAbissaisEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        int castState = entity.getCastState();
        if (castState == LordeDasMaresAbissaisEntity.CAST_ABYSSAL_TRIDENT) {
            this.rightArm.xRot = -1.45F + Mth.sin(ageInTicks * 0.35F) * 0.12F;
            this.leftArm.xRot = -0.65F;
        } else if (castState == LordeDasMaresAbissaisEntity.CAST_TIDAL_CURRENTS
                || castState == LordeDasMaresAbissaisEntity.CAST_SUMMON_TIDES) {
            this.rightArm.xRot = -1.85F + Mth.sin(ageInTicks * 0.25F) * 0.15F;
            this.leftArm.xRot = -1.75F - Mth.sin(ageInTicks * 0.25F) * 0.15F;
        } else {
            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.85F * limbSwingAmount - 0.25F;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.85F * limbSwingAmount - 0.15F;
        }

        float wave = ageInTicks * 0.14F + limbSwing * 0.45F;
        this.tailUpper.xRot = 0.20F + Mth.sin(wave) * 0.12F;
        this.tailMid.xRot = 0.32F + Mth.sin(wave - 0.6F) * 0.18F;
        this.tailFin.xRot = 0.25F + Mth.sin(wave - 1.2F) * 0.24F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
