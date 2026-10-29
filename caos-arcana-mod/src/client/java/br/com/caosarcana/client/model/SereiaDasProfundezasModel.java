package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.SereiaDasProfundezasEntity;
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
public final class SereiaDasProfundezasModel extends EntityModel<SereiaDasProfundezasEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("sereia_das_profundezas"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/recife_coral_abissal/sereia_das_profundezas.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart torso;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart tailUpper;
    private final ModelPart tailMid;
    private final ModelPart tailLower;
    private final ModelPart tailFin;

    public SereiaDasProfundezasModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.torso = root.getChild("torso");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.tailUpper = root.getChild("tail_upper");
        this.tailMid = this.tailUpper.getChild("tail_mid");
        this.tailLower = this.tailMid.getChild("tail_lower");
        this.tailFin = this.tailLower.getChild("tail_fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Cabeça com coroa abissal dourada e nadadeiras laterais
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(36, 0).addBox(-4.5F, -11.0F, -4.5F, 9.0F, 4.0F, 9.0F)
                        .texOffs(76, 0).addBox(-5.0F, -7.0F, -1.0F, 1.0F, 5.0F, 4.0F)
                        .texOffs(88, 0).addBox(4.0F, -7.0F, -1.0F, 1.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        // 2. Torso escamado com pérola luminosa no peito e fitas bioluminescentes dorsais
        root.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 10.0F, 4.0F)
                        .texOffs(26, 20).addBox(-3.0F, 0.5F, 2.0F, 6.0F, 10.0F, 2.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        // 3. Braços com membranas/nadadeiras e garras cianas
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-3.0F, -1.5F, -1.5F, 3.0F, 11.0F, 3.0F)
                        .texOffs(14, 38).addBox(-4.0F, 0.5F, -0.5F, 1.0F, 8.0F, 3.0F),
                PartPose.offset(-3.5F, 2.5F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(26, 38).addBox(0.0F, -1.5F, -1.5F, 3.0F, 11.0F, 3.0F)
                        .texOffs(40, 38).addBox(3.0F, 0.5F, -0.5F, 1.0F, 8.0F, 3.0F),
                PartPose.offset(3.5F, 2.5F, 0.0F));

        // 4. Cauda serpentina escamada com nadadeira caudal bifurcada
        PartDefinition tailUpper = root.addOrReplaceChild("tail_upper",
                CubeListBuilder.create()
                        .texOffs(0, 56).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition tailMid = tailUpper.addOrReplaceChild("tail_mid",
                CubeListBuilder.create()
                        .texOffs(22, 56).addBox(-2.5F, 0.0F, -1.5F, 5.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 5.5F, 0.5F));

        PartDefinition tailLower = tailMid.addOrReplaceChild("tail_lower",
                CubeListBuilder.create()
                        .texOffs(40, 56).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, 5.5F, 1.0F));

        tailLower.addOrReplaceChild("tail_fin",
                CubeListBuilder.create()
                        .texOffs(54, 56).addBox(-4.5F, 0.0F, -0.5F, 9.0F, 6.0F, 1.0F),
                PartPose.offset(0.0F, 4.5F, 0.5F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(SereiaDasProfundezasEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.9F * limbSwingAmount - 0.15F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.9F * limbSwingAmount - 0.15F;

        float swimWave = ageInTicks * 0.18F + limbSwing * 0.5F;
        this.tailUpper.xRot = 0.25F + Mth.sin(swimWave) * 0.15F;
        this.tailMid.xRot = 0.35F + Mth.sin(swimWave - 0.6F) * 0.22F;
        this.tailLower.xRot = 0.40F + Mth.sin(swimWave - 1.2F) * 0.28F;
        this.tailFin.xRot = 0.25F + Mth.sin(swimWave - 1.7F) * 0.32F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
