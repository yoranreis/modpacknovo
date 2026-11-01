package br.com.caosarcana.mixin;

import br.com.caosarcana.common.talent.CaosArcanaTalentBridge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.puffish.skillsmod.SkillsMod", remap = false)
public class PuffishSkillsModMixin {
    @Inject(method = "tryUnlockSkill", at = @At("HEAD"), cancellable = true, remap = false)
    private void caosarcana$interceptPreUnlock(ServerPlayer player, ResourceLocation categoryId, String skillId, boolean deductPoints, CallbackInfo ci) {
        if (!CaosArcanaTalentBridge.canUnlock(player, categoryId, skillId)) {
            ci.cancel();
            try {
                net.puffish.skillsmod.SkillsMod.getInstance().updateAllCategories(player);
            } catch (Throwable ignored) {}
        }
    }
}
