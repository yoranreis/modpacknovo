package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.AnciaoDasSombrasEntity;
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
public final class AnciaoDasSombrasModel extends EntityModel<AnciaoDasSombrasEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("anciao_das_sombras"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/floresta_negra/anciao_das_sombras.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart body;
    private final ModelPart soulCore;
    private final ModelPart soulLightning;
    private final ModelPart backSoulPlume;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public AnciaoDasSombrasModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.body = root.getChild("body");
        this.soulCore = this.body.getChild("soul_core");
        this.soulLightning = this.body.getChild("soul_lightning");
        this.backSoulPlume = this.body.getChild("back_soul_plume");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cabeça de tronco/crânio ancestral com anéis de árvore no topo e olhos verde-esmeralda
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 7.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));

        // Mandíbula articulada com dentes de madeira
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(36, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, -1.0F, -0.5F));

        // Galhos retorcidos na cabeça
        head.addOrReplaceChild("head_left_branch", CubeListBuilder.create()
                        .texOffs(68, 0).addBox(-7.5F, -11.0F, -1.0F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.15F, 0.0F, -0.35F));
        head.addOrReplaceChild("head_right_branch", CubeListBuilder.create()
                        .texOffs(82, 0).addBox(4.5F, -11.0F, -1.0F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.15F, 0.0F, 0.35F));
        head.addOrReplaceChild("head_crown_twigs", CubeListBuilder.create()
                        .texOffs(96, 0).addBox(-3.5F, -11.5F, 1.0F, 7.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.25F, 0.0F, 0.0F));

        // Torso de costelas de madeira negra envolvendo o núcleo de alma
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-6.0F, -12.0F, -4.0F, 12.0F, 9.0F, 8.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        body.addOrReplaceChild("waist", CubeListBuilder.create()
                        .texOffs(42, 16).addBox(-4.5F, -3.0F, -3.0F, 9.0F, 5.0F, 6.0F),
                PartPose.ZERO);

        // Núcleo de Alma verde-esmeralda pulsante no centro do peito
        body.addOrReplaceChild("soul_core", CubeListBuilder.create()
                        .texOffs(74, 16).addBox(-2.5F, -9.5F, -4.3F, 5.0F, 5.0F, 3.0F),
                PartPose.ZERO);

        // Raios/chamas de energia verde saindo do núcleo em direção ao ombro esquerdo
        body.addOrReplaceChild("soul_lightning", CubeListBuilder.create()
                        .texOffs(92, 16).addBox(-7.0F, -11.5F, -4.55F, 14.0F, 8.0F, 1.0F),
                PartPose.ZERO);

        // Ombreiras de casca grossa e galhos dorsais
        body.addOrReplaceChild("right_pauldron", CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-10.5F, -13.5F, -4.5F, 6.0F, 5.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.22F));
        body.addOrReplaceChild("left_pauldron", CubeListBuilder.create()
                        .texOffs(32, 34).addBox(4.5F, -13.5F, -4.5F, 6.0F, 5.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.22F));

        body.addOrReplaceChild("back_left_branch", CubeListBuilder.create()
                        .texOffs(64, 34).addBox(2.5F, -20.0F, 2.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.35F, 0.15F, 0.28F));
        body.addOrReplaceChild("back_right_branch", CubeListBuilder.create()
                        .texOffs(78, 34).addBox(-5.5F, -20.0F, 2.5F, 3.0F, 10.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.35F, -0.15F, -0.28F));

        // Trilhas espectrais verdes e centelhas nas costas
        body.addOrReplaceChild("back_soul_plume", CubeListBuilder.create()
                        .texOffs(92, 34).addBox(-7.0F, -19.0F, 4.2F, 14.0F, 12.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.18F, 0.0F, 0.0F));

        // Braço direito colossal com cipós e garras articuladas
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 50).addBox(-3.5F, -2.0F, -2.5F, 5.0F, 13.0F, 5.0F),
                PartPose.offset(-7.5F, 2.5F, 0.0F));
        rightArm.addOrReplaceChild("right_vine_wrap", CubeListBuilder.create()
                        .texOffs(44, 50).addBox(-3.8F, 4.0F, -2.8F, 6.0F, 5.0F, 6.0F),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("right_claw_outer", CubeListBuilder.create()
                        .texOffs(70, 50).addBox(-3.5F, 10.5F, -3.2F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.35F, 0.0F, 0.1F));
        rightArm.addOrReplaceChild("right_claw_mid", CubeListBuilder.create()
                        .texOffs(80, 50).addBox(-1.2F, 10.5F, -3.4F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("right_claw_inner", CubeListBuilder.create()
                        .texOffs(90, 50).addBox(0.2F, 9.5F, -1.5F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.25F, 0.0F, -0.25F));

        // Braço esquerdo colossal com cipós e garras articuladas
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(22, 50).addBox(-1.5F, -2.0F, -2.5F, 5.0F, 13.0F, 5.0F),
                PartPose.offset(7.5F, 2.5F, 0.0F));
        leftArm.addOrReplaceChild("left_vine_wrap", CubeListBuilder.create()
                        .texOffs(44, 50).addBox(-2.2F, 4.0F, -2.8F, 6.0F, 5.0F, 6.0F),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("left_claw_outer", CubeListBuilder.create()
                        .texOffs(70, 50).addBox(1.5F, 10.5F, -3.2F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.35F, 0.0F, -0.1F));
        leftArm.addOrReplaceChild("left_claw_mid", CubeListBuilder.create()
                        .texOffs(80, 50).addBox(-0.8F, 10.5F, -3.4F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("left_claw_inner", CubeListBuilder.create()
                        .texOffs(90, 50).addBox(-2.2F, 9.5F, -1.5F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.25F, 0.0F, 0.25F));

        // Pernas de tronco enraizadas
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 70).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                PartPose.offset(-3.2F, 14.0F, 0.0F));
        rightLeg.addOrReplaceChild("right_root_base", CubeListBuilder.create()
                        .texOffs(44, 70).addBox(-4.0F, 7.0F, -4.5F, 7.0F, 3.0F, 8.0F),
                PartPose.ZERO);

        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(22, 70).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                PartPose.offset(3.2F, 14.0F, 0.0F));
        leftLeg.addOrReplaceChild("left_root_base", CubeListBuilder.create()
                        .texOffs(76, 70).addBox(-3.0F, 7.0F, -4.5F, 7.0F, 3.0F, 8.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(AnciaoDasSombrasEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float walk = limbSwing * 0.55F;
        float stride = limbSwingAmount * 0.55F;
        rightLeg.xRot = Mth.cos(walk) * stride;
        leftLeg.xRot = Mth.cos(walk + (float) Math.PI) * stride;

        // Postura imponente das garras
        rightArm.xRot = Mth.cos(walk + (float) Math.PI) * stride - 0.35F;
        leftArm.xRot = Mth.cos(walk) * stride - 0.35F;
        rightArm.zRot = 0.14F + Mth.sin(ageInTicks * 0.06F) * 0.04F;
        leftArm.zRot = -0.14F - Mth.sin(ageInTicks * 0.06F) * 0.04F;

        // Mandíbula respirando ou rugindo durante conjurações
        int castState = entity.getCastState();
        if (castState != AnciaoDasSombrasEntity.CAST_NONE) {
            jaw.xRot = 0.38F + Mth.sin(ageInTicks * 0.45F) * 0.12F;
            if (castState == AnciaoDasSombrasEntity.CAST_ROOT_SNARE) {
                // Crava as garras para baixo invocando raízes
                rightArm.xRot = -1.25F + Mth.sin(ageInTicks * 0.35F) * 0.15F;
                leftArm.xRot = -1.25F - Mth.sin(ageInTicks * 0.35F) * 0.15F;
            } else if (castState == AnciaoDasSombrasEntity.CAST_SHADOW_CLONES) {
                // Abre os braços invocando sombras duplicadas
                rightArm.xRot = -0.85F;
                leftArm.xRot = -0.85F;
                rightArm.zRot = 0.65F;
                leftArm.zRot = -0.65F;
            } else if (castState == AnciaoDasSombrasEntity.CAST_LEAF_STORM) {
                // Ergue ambos os braços para o alto conjurando a tempestade de folhas
                rightArm.xRot = -2.1F + Mth.sin(ageInTicks * 0.5F) * 0.15F;
                leftArm.xRot = -2.1F - Mth.sin(ageInTicks * 0.5F) * 0.15F;
            }
        } else {
            jaw.xRot = 0.12F + Mth.sin(ageInTicks * 0.08F) * 0.06F;
            if (entity.isAggressive()) {
                rightArm.xRot = -0.65F + Mth.sin(ageInTicks * 0.15F) * 0.08F;
                leftArm.xRot = -0.65F - Mth.sin(ageInTicks * 0.15F) * 0.08F;
            }
        }

        // Pulsação do núcleo de alma e das chamas dorsais
        float pulse = 1.0F + Mth.sin(ageInTicks * 0.18F) * 0.08F;
        soulCore.xScale = pulse;
        soulCore.yScale = pulse;
        soulLightning.zRot = Mth.sin(ageInTicks * 0.12F) * 0.04F;
        backSoulPlume.yRot = Mth.sin(ageInTicks * 0.1F) * 0.08F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
