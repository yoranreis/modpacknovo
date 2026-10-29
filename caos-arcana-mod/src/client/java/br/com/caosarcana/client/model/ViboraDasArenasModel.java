package br.com.caosarcana.client.model;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.entity.ViboraDasArenasEntity;
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
public final class ViboraDasArenasModel extends EntityModel<ViboraDasArenasEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(CaosArcana.id("vibora_das_arenas"), "main");
    public static final ResourceLocation TEXTURE_LOCATION =
            CaosArcana.id("textures/entity/deserto_dunas_douradas/vibora_das_arenas.png");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart neckUpper;
    private final ModelPart neckMid;
    private final ModelPart neckLower;
    private final ModelPart bodyBase;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tailTip;

    public ViboraDasArenasModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.head = root.getChild("head");
        this.neckUpper = root.getChild("neck_upper");
        this.neckMid = root.getChild("neck_mid");
        this.neckLower = root.getChild("neck_lower");
        this.bodyBase = root.getChild("body_base");
        this.tail1 = this.bodyBase.getChild("tail_1");
        this.tail2 = this.tail1.getChild("tail_2");
        this.tailTip = this.tail2.getChild("tail_tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cabeça triangular de víbora com focinho e presas brancas
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -2.0F, -4.0F, 7.0F, 4.0F, 5.0F)
                        .texOffs(26, 0).addBox(-2.5F, -1.2F, -7.5F, 5.0F, 3.0F, 4.0F)
                        .texOffs(46, 0).addBox(-2.0F, 1.8F, -7.0F, 1.0F, 2.0F, 1.0F)
                        .texOffs(52, 0).addBox(1.0F, 1.8F, -7.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 9.5F, -4.5F));

        // Pescoço erguido em curva S vertical (postura de bote)
        root.addOrReplaceChild("neck_upper", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 13.0F, -3.0F, 0.35F, 0.0F, 0.0F));

        root.addOrReplaceChild("neck_mid", CubeListBuilder.create()
                        .texOffs(22, 12).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, -3.8F, -0.22F, 0.0F, 0.0F));

        root.addOrReplaceChild("neck_lower", CubeListBuilder.create()
                        .texOffs(44, 12).addBox(-3.0F, -5.0F, -2.5F, 6.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 22.5F, -2.5F, 0.18F, 0.0F, 0.0F));

        // Corpo e cauda segmentada rastejando na areia
        PartDefinition bodyBase = root.addOrReplaceChild("body_base", CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-3.0F, -2.5F, 0.0F, 6.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, 21.5F, -1.0F));

        PartDefinition tail1 = bodyBase.addOrReplaceChild("tail_1", CubeListBuilder.create()
                        .texOffs(28, 26).addBox(-2.5F, -2.2F, 0.0F, 5.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, 0.3F, 6.5F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail_2", CubeListBuilder.create()
                        .texOffs(54, 26).addBox(-2.0F, -1.8F, 0.0F, 4.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 0.4F, 6.5F));

        tail2.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                        .texOffs(76, 26).addBox(-1.5F, -1.2F, 0.0F, 3.0F, 2.0F, 5.0F),
                PartPose.offset(0.0F, 0.4F, 5.5F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(ViboraDasArenasEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F) * 0.7F;
        head.xRot = headPitch * ((float) Math.PI / 180.0F) * 0.5F;

        float slither = ageInTicks * 0.18F + limbSwing * 0.85F;
        float amp = 0.18F + limbSwingAmount * 0.35F;

        neckUpper.zRot = Mth.sin(slither) * 0.08F;
        neckMid.zRot = Mth.sin(slither - 0.6F) * 0.08F;
        bodyBase.yRot = Mth.sin(slither - 1.0F) * amp * 0.6F;
        tail1.yRot = Mth.sin(slither - 1.8F) * amp * 0.9F;
        tail2.yRot = Mth.sin(slither - 2.6F) * amp * 1.1F;
        tailTip.yRot = Mth.sin(slither - 3.4F) * amp * 1.3F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
