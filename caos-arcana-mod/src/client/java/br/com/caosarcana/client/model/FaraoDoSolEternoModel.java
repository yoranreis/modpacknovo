package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.FaraoDoSolEternoEntity;
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
public final class FaraoDoSolEternoModel extends EntityModel<FaraoDoSolEternoEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("farao_do_sol_eterno"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/deserto_dunas_douradas/farao_do_sol_eterno.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart staffSunRays;

    public FaraoDoSolEternoModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.staffSunRays = this.rightArm.getChild("staff_sun_rays");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Máscara mortuária de ouro com barba cerimonial, cobra Uraeus e touca real Nemes azul/ouro
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-1.0F, -1.0F, -4.8F, 2.0F, 4.0F, 2.0F)
                        .texOffs(40, 0).addBox(-1.0F, -10.0F, -5.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(50, 0).addBox(-5.0F, -9.2F, -4.5F, 10.0F, 4.0F, 9.0F)
                        .texOffs(88, 0).addBox(-6.0F, -8.0F, -4.2F, 12.0F, 9.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Torso real enfaixado com abas do Nemes e túnica azul/dourada com estola central
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-5.0F, -0.5F, -3.2F, 10.0F, 6.0F, 2.0F)
                        .texOffs(24, 18).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F)
                        .texOffs(50, 18).addBox(-4.5F, 9.5F, -3.0F, 9.0F, 10.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Braço direito empunhando o Cajado do Sol Eterno
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 36).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                        .texOffs(32, 36).addBox(-3.5F, 4.0F, -2.5F, 5.0F, 4.0F, 5.0F)
                        // Haste dourada do Cajado do Sol Eterno
                        .texOffs(72, 36).addBox(-1.5F, -12.0F, -5.0F, 1.0F, 26.0F, 1.0F)
                        // Berço crescente dourado
                        .texOffs(76, 36).addBox(-3.5F, -14.0F, -5.0F, 5.0F, 4.0F, 1.0F)
                        // Núcleo solar incandescente
                        .texOffs(108, 36).addBox(-2.5F, -18.0F, -5.5F, 3.0F, 3.0F, 2.0F),
                PartPose.offset(-5.5F, 2.0F, 0.0F));

        // Disco solar radiante com pontas no topo do cajado
        rightArm.addOrReplaceChild("staff_sun_rays", CubeListBuilder.create()
                        .texOffs(88, 36).addBox(-4.5F, -4.5F, -0.5F, 9.0F, 9.0F, 1.0F),
                PartPose.offset(-1.0F, -16.5F, -4.5F));

        // Braço esquerdo com braceletes de ouro (cruzado sobre o peito em postura real)
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(16, 36).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                        .texOffs(52, 36).addBox(-1.5F, 4.0F, -2.5F, 5.0F, 4.0F, 5.0F),
                PartPose.offset(5.5F, 2.0F, 0.0F));

        // Pernas enfaixadas com sandálias douradas
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 64).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.1F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(16, 64).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.1F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(FaraoDoSolEternoEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float walk = limbSwing * 0.55F;
        float stride = limbSwingAmount * 0.5F;
        rightLeg.xRot = Mth.cos(walk) * stride;
        leftLeg.xRot = Mth.cos(walk + (float) Math.PI) * stride;

        // Postura real: braço esquerdo dobrado sobre o peito e braço direito segurando o Cajado Solar
        leftArm.xRot = -0.85F;
        leftArm.yRot = -0.45F;
        rightArm.xRot = -0.45F + Mth.sin(ageInTicks * 0.08F) * 0.05F;
        rightArm.yRot = 0.0F;

        int castState = entity.getCastState();
        if (castState == FaraoDoSolEternoEntity.CAST_SOLAR_BEAM) {
            // Aponta o Cajado do Sol Eterno para frente canalizando o Raio Solar
            rightArm.xRot = -1.35F + Mth.sin(ageInTicks * 0.4F) * 0.08F;
            staffSunRays.zRot = ageInTicks * 0.25F;
        } else if (castState == FaraoDoSolEternoEntity.CAST_SAND_WHIRLWIND) {
            // Gira o cajado conjurando o Redemoinho de Areia
            rightArm.xRot = -1.65F;
            rightArm.zRot = Mth.sin(ageInTicks * 0.45F) * 0.25F;
            staffSunRays.zRot = ageInTicks * 0.35F;
        } else if (castState == FaraoDoSolEternoEntity.CAST_SUMMON_GUARDIANS) {
            // Ergue ambos os braços invocando os Guardiões Múmias
            rightArm.xRot = -2.0F;
            leftArm.xRot = -2.0F;
            leftArm.yRot = 0.0F;
            staffSunRays.zRot = ageInTicks * 0.2F;
        } else {
            rightArm.zRot = 0.0F;
            staffSunRays.zRot = Mth.sin(ageInTicks * 0.06F) * 0.1F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
