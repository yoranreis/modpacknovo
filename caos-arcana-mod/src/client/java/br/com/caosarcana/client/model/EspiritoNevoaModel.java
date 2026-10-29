package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.EspiritoNevoaEntity;
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
public final class EspiritoNevoaModel extends EntityModel<EspiritoNevoaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("espirito_nevoa"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/floresta_negra/espirito_nevoa.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart torso;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart tailUpper;
    private final ModelPart tailMid;
    private final ModelPart tailLower;

    public EspiritoNevoaModel(ModelPart root) {
        super(net.minecraft.client.renderer.RenderType::entityTranslucent);
        this.root = root;
        this.head = root.getChild("head");
        this.torso = root.getChild("torso");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.tailUpper = root.getChild("tail_upper");
        this.tailMid = this.tailUpper.getChild("tail_mid");
        this.tailLower = this.tailMid.getChild("tail_lower");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Cabeça (Crânio espectral com mandíbula)
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F)
                        .texOffs(28, 0).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 4.0F, -2.0F));

        // 2. Tronco / Caixa Torácica (com núcleo de alma e plumas nas costas)
        PartDefinition torso = root.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(0, 14).addBox(-4.0F, -4.0F, -3.0F, 8.0F, 8.0F, 6.0F)
                        .texOffs(28, 14).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                        .texOffs(0, 28).addBox(-3.0F, -6.0F, 1.0F, 6.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        // 3. Braço Direito com garras fantasmagóricas
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(44, 0).addBox(-3.0F, -1.0F, -2.0F, 3.0F, 6.0F, 4.0F)
                        .texOffs(44, 10).addBox(-3.0F, 4.0F, -5.0F, 3.0F, 4.0F, 6.0F),
                PartPose.offset(-4.5F, 5.0F, 0.0F));

        // 4. Braço Esquerdo com garras fantasmagóricas
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(44, 20).addBox(0.0F, -1.0F, -2.0F, 3.0F, 6.0F, 4.0F)
                        .texOffs(44, 30).addBox(0.0F, 4.0F, -5.0F, 3.0F, 4.0F, 6.0F),
                PartPose.offset(4.5F, 5.0F, 0.0F));

        // 5. Cauda articulada de névoa (substitui as pernas)
        PartDefinition tailUpper = root.addOrReplaceChild("tail_upper",
                CubeListBuilder.create()
                        .texOffs(0, 35).addBox(-3.0F, 0.0F, -2.5F, 6.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition tailMid = tailUpper.addOrReplaceChild("tail_mid",
                CubeListBuilder.create()
                        .texOffs(22, 35).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 4.5F, 0.5F));

        tailMid.addOrReplaceChild("tail_lower",
                CubeListBuilder.create()
                        .texOffs(38, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, 4.5F, 0.5F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(EspiritoNevoaEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float bob = (float) Math.sin(ageInTicks * 0.1F) * 1.2F;

        // Flutuação global do corpo espectral
        this.torso.y = 8.0F + bob;
        this.head.y = 4.0F + bob;
        this.rightArm.y = 5.0F + bob;
        this.leftArm.y = 5.0F + bob;
        this.tailUpper.y = 11.0F + bob;

        // Postura curvada para frente (semelhante à arte de referência)
        this.torso.xRot = 0.2F;

        // Cabeça acompanha o jogador
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F) + 0.1F;

        // Braços flutuando à frente como quem rasteja pelo ar
        this.rightArm.xRot = -0.35F + (float) Math.sin(ageInTicks * 0.08F) * 0.07F;
        this.leftArm.xRot = -0.35F - (float) Math.sin(ageInTicks * 0.08F) * 0.07F;
        this.rightArm.zRot = 0.12F;
        this.leftArm.zRot = -0.12F;

        // Cauda esvoaçando suavemente como ectoplasma
        this.tailUpper.xRot = 0.35F + (float) Math.sin(ageInTicks * 0.12F) * 0.08F;
        this.tailMid.xRot = 0.3F + (float) Math.sin(ageInTicks * 0.12F - 0.5F) * 0.12F;
        this.tailMid.yRot = (float) Math.cos(ageInTicks * 0.09F) * 0.1F;
        this.tailLower.xRot = 0.35F + (float) Math.sin(ageInTicks * 0.12F - 1.0F) * 0.15F;
        this.tailLower.yRot = (float) Math.cos(ageInTicks * 0.09F - 0.5F) * 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        // Efeito translúcido etéreo (alfa 0.88F)
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, 0.88F);
    }
}
