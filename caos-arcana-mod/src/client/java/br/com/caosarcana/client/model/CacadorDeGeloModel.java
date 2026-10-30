package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.CacadorDeGeloEntity;
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
public final class CacadorDeGeloModel extends EntityModel<CacadorDeGeloEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("cacador_de_gelo"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/muralha_de_gelo/cacador_de_gelo.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private boolean camouflaged = false;

    public CacadorDeGeloModel(ModelPart root) {
        super(RenderType::entityTranslucent);
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

        // 1. Cabeça sombreada + Capuz espesso de pele branca com pingentes de gelo
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(36, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 2. Torso com núcleo ciano no peitoral, aljava nas costas e saiote de pele
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
                        .texOffs(26, 20).addBox(-2.5F, 1.0F, 2.0F, 5.0F, 9.0F, 3.0F)
                        .texOffs(44, 20).addBox(0.5F, -3.0F, 2.5F, 3.0F, 4.0F, 1.0F)
                        .texOffs(54, 20).addBox(-4.0F, 10.5F, -2.5F, 8.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 3. Braço Direito empunhando a Lança de Gelo Cristalino Afiado
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        rightArm.addOrReplaceChild("ice_spear",
                CubeListBuilder.create()
                        .texOffs(80, 0).addBox(-1.5F, -10.0F, -5.0F, 1.0F, 22.0F, 1.0F)
                        .texOffs(86, 0).addBox(-2.5F, -17.0F, -5.0F, 3.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        // 4. Braço Esquerdo
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(18, 40).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        // 5. Pernas com botas de pele
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(18, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(CacadorDeGeloEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.camouflaged = entity.isCamouflaged();
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.1F * limbSwingAmount - 0.35F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        float effectiveAlpha = this.camouflaged ? 0.28F : alpha;
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, effectiveAlpha);
    }
}
