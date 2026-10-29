package br.com.caosarcana.content.trials;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.content.spells.SpellDefinition;
import br.com.caosarcana.content.spells.SpellRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.*;

/**
 * Registro de Provações Arcanas (Arcane Trials).
 * Permite que jogadores desbloqueiem magias universais por mérito, missões,
 * rituais e desafios de dungeon.
 */
public class ArcaneTrialRegistry {
    public record Trial(
            String id,
            String name,
            String school,
            int requiredLevel,
            String rewardSpellId,
            String description,
            String trialInstructions
    ) {}

    private static final Map<String, Trial> TRIALS = new LinkedHashMap<>();

    static {
        register(new Trial(
                "fogo",
                "Provação das Chamas",
                "piromancia",
                1,
                "caosarcana:centelha_ignea",
                "Canalização e sobrevivência ao calor primordial do fogo arcano.",
                "Enfrente criaturas flamejantes no Nether ou medite em chamas sagradas."
        ));

        register(new Trial(
                "gelo",
                "Provação da Geada Eterna",
                "criomancia",
                1,
                "caosarcana:geada_congelante",
                "Domínio do frio cortante dos picos nevados de Skyrim.",
                "Sobreviva à nevasca nos cumes mais altos e controle o congelamento."
        ));

        register(new Trial(
                "trovao",
                "Provação dos Céus Eletrizados",
                "eletromancia",
                2,
                "caosarcana:relampago_arcano",
                "Absorção e disparo de arcos voltaicos durante tempestades.",
                "Canalize o poder dos relâmpagos em noites tempestuosas."
        ));

        register(new Trial(
                "cura",
                "Provação do Toque Restaurador",
                "restauracao",
                1,
                "caosarcana:toque_de_cura",
                "Canalização de misericórdia e restauração da vitalidade perdida.",
                "Sobreviva a ferimentos severos e cure a si mesmo ou aliados."
        ));

        register(new Trial(
                "sagrada",
                "Provação do Círculo Sagrado",
                "restauracao",
                2,
                "caosarcana:circulo_sagrado",
                "Purificação dos mortos-vivos e invocação de barreiras sagradas.",
                "Expulse hordas de mortos-vivos em catacumbas e masmorras profundas."
        ));

        register(new Trial(
                "carvalho",
                "Provação da Casca de Carvalho",
                "alteracao",
                1,
                "caosarcana:corpo_de_carvalho",
                "Compreensão da densidade física e alteração da estrutura da pele.",
                "Resista a impactos massivos em cavernas sem quebrar suas defesas."
        ));

        register(new Trial(
                "luz",
                "Provação da Luz Guia",
                "alteracao",
                1,
                "caosarcana:luz_de_vela",
                "Convocação da luz perpétua nas trevas do abismo.",
                "Explore as profundezas escuras do mundo sem tochas convencionais."
        ));

        register(new Trial(
                "espada",
                "Provação da Lâmina Espectral",
                "conjuracao",
                1,
                "caosarcana:espada_vinculada",
                "Ruptura dos limites de Oblivion para materializar armas arcanas.",
                "Derrote adversários canalizando seu espírito marcial puro."
        ));

        register(new Trial(
                "familiar",
                "Provação do Espírito Familiar",
                "conjuracao",
                2,
                "caosarcana:familiar_espectral",
                "Pacto místico de lealdade com as matilhas astrais de caça.",
                "Encontre um lobo na natureza sob o luar e estabeleça vínculo de alma."
        ));

        register(new Trial(
                "sombras",
                "Provação do Véu das Sombras",
                "ilusao",
                1,
                "caosarcana:manto_de_sombras",
                "Distorção da percepção alheia e mimetização perfeita com o escuro.",
                "Passe despercebido por monstros hostis sem ser detectado."
        ));
    }

    public static Trial register(Trial trial) {
        TRIALS.put(trial.id().toLowerCase(), trial);
        return trial;
    }

    public static Optional<Trial> get(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(TRIALS.get(id.trim().toLowerCase()));
    }

    public static Collection<Trial> getAll() {
        return Collections.unmodifiableCollection(TRIALS.values());
    }

    /**
     * Completa uma provação com sucesso para o jogador, concedendo a magia universal.
     */
    public static boolean completeTrial(ServerPlayer player, String trialId) {
        if (!(player instanceof IRPGPlayerData rpgPlayer)) return false;
        Optional<Trial> opt = get(trialId);
        if (opt.isEmpty()) return false;

        Trial trial = opt.get();
        String spellId = trial.rewardSpellId();

        boolean alreadyKnown = rpgPlayer.caosarcana$knowsSpell(spellId);
        boolean learned = rpgPlayer.caosarcana$learnSpell(spellId);

        if (learned || alreadyKnown) {
            ModMessages.syncPlayerData(player);

            ServerLevel level = (ServerLevel) player.level();
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2, player.getZ(), 30, 0.5, 0.6, 0.5, 0.15);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);

            Optional<SpellDefinition> spellDef = SpellRegistry.get(spellId);
            String spellName = spellDef.map(SpellDefinition::name).orElse(spellId);

            player.sendSystemMessage(Component.literal("§6=================================================="));
            player.sendSystemMessage(Component.literal("§6§l✦ PROVAÇÃO ARCANA CONCLUÍDA: §e" + trial.name() + " §6§l✦"));
            player.sendSystemMessage(Component.literal("§aVocê provou sua mestria na escola de §f" + trial.school() + "§a!"));
            player.sendSystemMessage(Component.literal("§dMagia Universal Conquistada: §e" + spellName));
            player.sendSystemMessage(Component.literal("§7" + trial.description()));
            player.sendSystemMessage(Component.literal("§6=================================================="));
            return true;
        }

        return false;
    }
}
