package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.GolemDeAreiaEntity;
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
public final class GolemDeAreiaModel extends EntityModel<GolemDeAreiaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("golem_de_areia"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/deserto_dunas_douradas/golem_de_areia.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GolemDeAreiaModel(ModelPart root) {
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

        // Cabeça de arenito estilo Golem com nariz saliente
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -10.0F, -4.5F, 8.0F, 10.0F, 8.0F)
                        .texOffs(32, 0).addBox(-1.0F, -3.5F, -6.5F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -3.0F, -1.5F));

        // Torso maciço de arenito com cintura estreita e arbustos secos nos ombros
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-8.0F, -16.0F, -5.0F, 16.0F, 11.0F, 10.0F)
                        .texOffs(52, 20).addBox(-5.0F, -5.0F, -3.5F, 10.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));

        body.addOrReplaceChild("left_shrub", CubeListBuilder.create()
                        .texOffs(88, 20).addBox(3.5F, -20.5F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.35F, 0.08F));
        body.addOrReplaceChild("right_shrub", CubeListBuilder.create()
                        .texOffs(104, 20).addBox(-7.5F, -19.5F, -1.5F, 4.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.35F, -0.08F));

        // Braços colossais de arenito
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-5.0F, -2.0F, -3.0F, 5.0F, 18.0F, 6.0F),
                PartPose.offset(-8.0F, -1.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(24, 42).addBox(0.0F, -2.0F, -3.0F, 5.0F, 18.0F, 6.0F),
                PartPose.offset(8.0F, -1.0F, 0.0F));

        // Pernas de pilares de arenito
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(48, 42).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 11.0F, 6.0F),
                PartPose.offset(-4.0F, 13.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(72, 42).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 11.0F, 6.0F),
                PartPose.offset(4.0F, 13.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(GolemDeAreiaEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float walk = limbSwing * 0.55F;
        float stride = limbSwingAmount * 0.65F;
        rightLeg.xRot = Mth.cos(walk) * stride;
        leftLeg.xRot = Mth.cos(walk + (float) Math.PI) * stride;

        if (entity.isSeismicWindup()) {
            // Ergue ambos os braços antes do Soco Sísmico
            rightArm.xRot = -2.2F + Mth.sin(ageInTicks * 0.5F) * 0.12F;
            leftArm.xRot = -2.2F - Mth.sin(ageInTicks * 0.5F) * 0.12F;
            rightArm.yRot = 0.0F;
            leftArm.yRot = 0.0F;
        } else {
            // Postura pesada com braços angulados à frente como na referência
            rightArm.xRot = -0.55F + Mth.cos(walk + (float) Math.PI) * stride * 0.7F;
            leftArm.xRot = -0.55F + Mth.cos(walk) * stride * 0.7F;
            rightArm.yRot = -0.25F;
            leftArm.yRot = 0.25F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
