package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.CultistaOcultoEntity;
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
public final class CultistaOcultoModel extends EntityModel<CultistaOcultoEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("cultista_oculto"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/floresta_negra/cultista_oculto.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart staffOrb;
    private final ModelPart leftWisp;
    private final ModelPart rightWisp;

    public CultistaOcultoModel(ModelPart root) {
        super(net.minecraft.client.renderer.RenderType::entityCutoutNoCull);
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.staffOrb = this.rightArm.getChild("staff").getChild("staff_orb");
        this.leftWisp = this.body.getChild("left_wisp");
        this.rightWisp = this.body.getChild("right_wisp");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Cabeça: Crânio verde brilhante recuado + Capuz profundo com recorte frontal em arco
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F)
                        .texOffs(32, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 2. Tronco, Manto Rasgado nos Ombros, Túnica Inferior e Base Interna
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F)
                        .texOffs(28, 18).addBox(-4.5F, -0.2F, -3.0F, 9.0F, 6.0F, 6.0F, new CubeDeformation(0.1F))
                        .texOffs(0, 34).addBox(-4.5F, 11.0F, -3.0F, 9.0F, 11.0F, 6.0F)
                        .texOffs(32, 34).addBox(-3.5F, 14.0F, -2.0F, 7.0F, 10.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Trilhas espectrais verdes fluindo das costas (como na arte de referência)
        body.addOrReplaceChild("left_wisp",
                CubeListBuilder.create()
                        .texOffs(60, 48).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 12.0F, 8.0F),
                PartPose.offsetAndRotation(2.5F, 4.0F, 2.5F, 0.25F, 0.35F, 0.0F));

        body.addOrReplaceChild("right_wisp",
                CubeListBuilder.create()
                        .texOffs(60, 48).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 12.0F, 8.0F),
                PartPose.offsetAndRotation(-2.5F, 4.0F, 2.5F, 0.2F, -0.35F, 0.0F));

        // 3. Braço Direito segurando o Cajado de Raízes com Orbe Verde
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 52).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 11.0F, 4.0F)
                        .texOffs(18, 52).addBox(-2.5F, 3.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.1F)),
                PartPose.offset(-5.5F, 2.0F, 0.0F));

        PartDefinition staff = rightArm.addOrReplaceChild("staff",
                CubeListBuilder.create()
                        .texOffs(70, 0).addBox(-1.0F, -10.0F, -4.5F, 2.0F, 24.0F, 2.0F)
                        .texOffs(80, 0).addBox(-1.5F, -12.0F, -5.0F, 3.0F, 3.0F, 3.0F)
                        .texOffs(80, 8).addBox(-3.5F, -18.0F, -4.5F, 2.0F, 7.0F, 2.0F)
                        .texOffs(90, 8).addBox(1.5F, -18.0F, -4.5F, 2.0F, 7.0F, 2.0F)
                        .texOffs(80, 18).addBox(-4.5F, -20.0F, -4.0F, 9.0F, 8.0F, 1.0F),
                PartPose.ZERO);

        staff.addOrReplaceChild("staff_orb",
                CubeListBuilder.create()
                        .texOffs(78, 30).addBox(-1.5F, -16.5F, -5.0F, 3.0F, 3.0F, 3.0F)
                        .texOffs(78, 38).addBox(-2.5F, -17.5F, -4.0F, 5.0F, 5.0F, 1.0F),
                PartPose.ZERO);

        // 4. Braço Esquerdo com gavinhas e raízes retorcidas saindo da manga
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 68).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 11.0F, 4.0F)
                        .texOffs(18, 68).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 9.0F, 5.0F, new CubeDeformation(0.1F)),
                PartPose.offset(5.5F, 2.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(CultistaOcultoEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        // Braço direito mantém o cajado erguido à frente como na referência
        this.rightArm.xRot = -0.35F + (float) Math.sin(ageInTicks * 0.08F) * 0.04F;
        this.rightArm.yRot = -0.1F;
        this.leftArm.xRot = 0.12F + (float) Math.cos(ageInTicks * 0.08F) * 0.05F;
        this.leftArm.zRot = -0.08F;

        // Orbe verde pulsando/flutuando no topo do cajado
        this.staffOrb.y = (float) Math.sin(ageInTicks * 0.15F) * 0.6F;

        // Ondulação das trilhas de energia verde nas costas
        this.leftWisp.yRot = 0.35F + (float) Math.sin(ageInTicks * 0.12F) * 0.12F;
        this.rightWisp.yRot = -0.35F - (float) Math.cos(ageInTicks * 0.12F) * 0.12F;

        // Balanço sutil ao caminhar
        float walk = limbSwing * 0.6F;
        float stride = Math.min(0.6F, limbSwingAmount);
        this.body.yRot = (float) Math.sin(walk) * 0.06F * stride;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
