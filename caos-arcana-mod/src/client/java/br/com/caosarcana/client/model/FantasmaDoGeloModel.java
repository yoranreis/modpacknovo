package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.FantasmaDoGeloEntity;
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
public final class FantasmaDoGeloModel extends EntityModel<FantasmaDoGeloEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("fantasma_do_gelo"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/muralha_de_gelo/fantasma_do_gelo.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart torso;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart tailUpper;
    private final ModelPart tailLower;
    private boolean intangible = false;

    public FantasmaDoGeloModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        this.head = root.getChild("head");
        this.torso = root.getChild("torso");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.tailUpper = root.getChild("tail_upper");
        this.tailLower = this.tailUpper.getChild("tail_lower");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Capuz de gelo translúcido com olhos cianos brilhantes
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 9.0F, 8.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));

        // 2. Torso translúcido de gelo com núcleo ciano e filamentos espectrais nas costas
        root.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 11.0F, 5.0F)
                        .texOffs(28, 20).addBox(-2.0F, 3.0F, -1.5F, 4.0F, 4.0F, 3.0F)
                        .texOffs(44, 20).addBox(-3.0F, 0.5F, 2.5F, 6.0F, 10.0F, 3.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));

        // 3. Braços espectrais com ombreiras de neve e estalactites
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-4.0F, -2.5F, -2.5F, 5.0F, 4.0F, 5.0F)
                        .texOffs(22, 38).addBox(-3.5F, 1.5F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offset(-4.5F, 3.5F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 56).addBox(-1.0F, -2.5F, -2.5F, 5.0F, 4.0F, 5.0F)
                        .texOffs(22, 56).addBox(-0.5F, 1.5F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offset(4.5F, 3.5F, 0.0F));

        // 4. Cauda fantasmagórica esvoaçante
        PartDefinition tailUpper = root.addOrReplaceChild("tail_upper",
                CubeListBuilder.create()
                        .texOffs(0, 74).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));

        tailUpper.addOrReplaceChild("tail_lower",
                CubeListBuilder.create()
                        .texOffs(24, 74).addBox(-2.5F, 0.0F, -1.5F, 5.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 5.5F, 0.5F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(FantasmaDoGeloEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.intangible = entity.isIntangible();
        float bob = Mth.sin(ageInTicks * 0.12F) * 1.1F;

        this.head.y = 2.0F + bob;
        this.torso.y = 2.0F + bob;
        this.rightArm.y = 3.5F + bob;
        this.leftArm.y = 3.5F + bob;
        this.tailUpper.y = 13.0F + bob;

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        this.rightArm.xRot = -0.35F + Mth.sin(ageInTicks * 0.1F) * 0.1F;
        this.leftArm.xRot = -0.35F - Mth.sin(ageInTicks * 0.1F) * 0.1F;

        this.tailUpper.xRot = 0.28F + Mth.sin(ageInTicks * 0.14F) * 0.12F;
        this.tailLower.xRot = 0.32F + Mth.sin(ageInTicks * 0.14F - 0.6F) * 0.18F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        float effectiveAlpha = this.intangible ? 0.45F : 0.86F;
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, effectiveAlpha);
    }
}
