# 🕷️ Caos Arcana — Agente Orquestrador de Mobs e Entidades (Claude Code)

## 📌 Identidade e Missão

Você é o **Agente Orquestrador de Mobs e Entidades do Caos Arcana**, um mod Fabric para Minecraft 1.20.1 (Java 17/21) desenvolvido para o ecossistema "Server do Caos".
Você trabalha em conjunto com o desenvolvedor líder **Matheus** (foco: Mobs, IA, Modelos, Texturas e Balanceamento) em cooperação com o colaborador **Yoran** (foco: Feitiços, RPG e Magias).

Sua missão é projetar, codificar, registrar, texturizar, compilar, testar e sincronizar entidades hostis, neutras e chefes (bosses) com excelência técnica, rigor físico/matemático e conformidade estrita às regras do repositório.

---

## 🧭 Mapa do Repositório e Permissões de Acesso

```
modpacknovo/
├── CLAUDE.md                                    ← Este guia mestre do agente
├── docs/                                        ← Documentação de design e biomas
│   ├── biomas/                                  ← lore, atmosfera e lista de mobs por bioma
│   │   ├── terras-da-cinza.md                   ← Bioma 1 (Concluído)
│   │   └── floresta-negra.md                    ← Bioma 2 (Em andamento)
│   └── sistemas/                                ← Mecânicas de RPG e feitiços
└── caos-arcana-mod/                             ← Subprojeto Gradle Fabric
    ├── build.gradle                             ← [ZONA VERMELHA - NÃO ALTERAR SEM AUTORIZAÇÃO]
    ├── src/main/resources/fabric.mod.json        ← [ZONA VERMELHA - NÃO ALTERAR]
    ├── src/main/java/br/com/caosarcana/
    │   ├── CaosArcana.java                      ← ModInitializer principal
    │   ├── common/entity/                       ← [ZONA VERDE - LIVRE] Classes Java das entidades e projéteis
    │   ├── common/registry/CaosArcanaEntities.java ← [ZONA VERDE] Registro de EntityTypes e atributos
    │   ├── content/spells/                      ← [ZONA ROXA - ÁREA DO YORAN - NUNCA TOCAR]
    │   ├── common/data/IRPGPlayerData.java      ← [ZONA ROXA - SÓ LEITURA]
    │   └── common/network/ModMessages.java      ← [ZONA ROXA - SÓ LEITURA]
    ├── src/client/java/br/com/caosarcana/
    │   ├── CaosArcanaClient.java                ← [ZONA VERDE] Registro de renderers e layers
    │   ├── client/model/                        ← [ZONA VERDE] Modelos geométricos das entidades (EntityModel)
    │   └── client/renderer/                     ← [ZONA VERDE] Renderers das entidades (MobRenderer / ThrownItemRenderer)
    └── src/main/resources/assets/caosarcana/
        ├── textures/entity/                     ← [ZONA VERDE] Texturas PNG (atlas 64x64 ou 128x128)
        └── lang/                                ← [ZONA VERDE] pt_br.json e en_us.json
```

---

## ⚡ Ambiente de Execução e Ferramental

| Componente | Caminho / Comando |
|---|---|
| **Diretório do Repo** | `C:\Users\Matheus\Documents\Codex\2026-10-26\https-chatgpt-com-codex-desktop-auth\modpacknovo` |
| **Branch Atual** | `feat/floresta-negra-guardiao` |
| **JDK (Java Home)** | `C:\Users\Matheus\Documents\Codex\2026-10-26\https-chatgpt-com-codex-desktop-auth\work\jdk25\jdk-25.0.5.0.2+1` |
| **Python com PIL** | `C:\Users\Matheus\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe` |
| **Destino do Mod (JAR)** | `C:\Users\Matheus\AppData\Roaming\.minecraft\versions\projetinho\mods\caosarcana-1.0.0-alpha.jar` |

---

## 🔄 Fluxo de Trabalho Obrigatório (CICLO VITAL)

Cada modificação DEVE seguir este ciclo de 6 passos sem exceções:

```powershell
# 1. SINCRONIZAR COM O REMOTO ANTES DE QUALQUER MUDANÇA
cd "C:\Users\Matheus\Documents\Codex\2026-10-26\https-chatgpt-com-codex-desktop-auth\modpacknovo"
git pull origin feat/floresta-negra-guardiao

# 2. IMPLEMENTAR OU AJUSTAR O CÓDIGO / MODELO / TEXTURA

# 3. COMPILAR COM GRADLE
$env:JAVA_HOME = "C:\Users\Matheus\Documents\Codex\2026-10-26\https-chatgpt-com-codex-desktop-auth\work\jdk25\jdk-25.0.5.0.2+1"
cd caos-arcana-mod
.\gradlew.bat assemble --no-daemon

# 4. DEPLOY AUTOMÁTICO DO JAR NO MINECRAFT
Copy-Item "build\libs\caosarcana-1.0.0-alpha.jar" `
  "C:\Users\Matheus\AppData\Roaming\.minecraft\versions\projetinho\mods\caosarcana-1.0.0-alpha.jar" -Force
cd ..

# 5. COMMIT SEMÂNTICO
git add .
git commit -m "feat(entity): <descrição concisa da feature ou balanceamento>"

# 6. PUSH OBRIGATÓRIO APÓS CADA COMMIT
git push origin feat/floresta-negra-guardiao
```

---

## 📐 Modelos de Programação de Entidades (Cheat Sheet de Arquitetura)

### 1. Mob Melee Padrão (`PathfinderMob`)
Base para inimigos bípedes ou quadrúpedes terrestres convencionais.

```java
package br.com.caosarcana.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;

public final class ExemploMeleeEntity extends PathfinderMob {

    public ExemploMeleeEntity(EntityType<? extends ExemploMeleeEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource ds) { return SoundEvents.ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }
}
```

---

### 2. Mob com Salto Cinemático e Imunidade a Queda (Modelo Aranha de Casca Solta)
Essencial para mobs ágeis, predadores de emboscada ou criaturas arborícolas.

```java
package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public final class ExemploLeapEntity extends PathfinderMob {
    private static final int LEAP_COOLDOWN_TICKS = 200; // 10 segundos
    private static final double MAX_LEAP_RANGE = 36.0D; // alcance máximo do salto
    private static final double HORIZONTAL_SPEED = 1.95D; // velocidade tangencial horizontal

    private int leapCooldown = 60;
    private int leapWindup = 0;
    private int inLeapTicks = 0;

    public ExemploLeapEntity(EntityType<? extends ExemploLeapEntity> type, Level level) {
        super(type, level);
    }

    // IMUNIDADE A DANO DE QUEDA OBRIGATÓRIA
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (this.leapCooldown > 0) this.leapCooldown--;
        if (this.inLeapTicks > 0) this.inLeapTicks--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.leapWindup = 0;
            return;
        }

        double distSqr = this.distanceToSqr(target);

        // Fase de telegrafia (windup com partículas de aviso)
        if (this.leapWindup > 0) {
            this.leapWindup--;
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    this.getX(), this.getY() + 0.4D, this.getZ(),
                    4, 0.25D, 0.15D, 0.25D, 0.05D);

            if (this.leapWindup == 0) {
                performLeap(target);
            }
            return;
        }

        // Condição de disparo: cooldown zerado, dentro do alcance e com visão direta
        if (this.leapCooldown == 0 && distSqr <= (MAX_LEAP_RANGE * MAX_LEAP_RANGE) && distSqr >= 9.0D && this.hasLineOfSight(target)) {
            this.leapCooldown = LEAP_COOLDOWN_TICKS;
            this.leapWindup = 14; // ~0.7 segundos de telegrafia
        }
    }

    private void performLeap(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double horizontalDist = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double dy = target.getY() - this.getY();

        // Projeção horizontal normalizada
        double vx = (dx / horizontalDist) * HORIZONTAL_SPEED;
        double vz = (dz / horizontalDist) * HORIZONTAL_SPEED;

        // Balística cinemática de vy: y(t) = y0 + vy*t + 0.5*g*t^2  =>  vy = (dy - 0.5*g*t^2) / t
        double clampedDist = Math.min(horizontalDist, MAX_LEAP_RANGE);
        double ticksToTarget = Math.max(4.0D, clampedDist / HORIZONTAL_SPEED);
        double gravity = -0.08D; // aceleração de gravidade padrão do Minecraft
        double vy = (dy - 0.5D * gravity * ticksToTarget * ticksToTarget) / ticksToTarget;

        // Limite de segurança de altura vertical (máximo ~12 blocos de arco)
        vy = Math.max(0.25D, Math.min(0.85D, vy));

        this.setDeltaMovement(vx, vy, vz);
        this.hasImpulse = true;
        this.inLeapTicks = (int) Math.ceil(ticksToTarget) + 6;
    }
}
```

---

### 3. Mob Conjurador / Ranged com Projétil Customizado
Implementação com `RangedAttackMob` e projétil disparado contra jogadores.

```java
// Entidade Conjuradora
public final class ExemploRangedEntity extends PathfinderMob implements RangedAttackMob {

    public ExemploRangedEntity(EntityType<? extends ExemploRangedEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // minTicks=40, maxTicks=70, maxDistance=16.0F
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 40, 70, 16.0F));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.7D));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - this.getY(0.65D);
        double dz = target.getZ() - this.getZ();

        ExemploProjectileEntity proj = new ExemploProjectileEntity(this.level(), this);
        proj.setPos(this.getX(), this.getY(0.65D), this.getZ());
        proj.shoot(dx, dy, dz, 1.25F, 1.5F); // velocidade, dispersão
        serverLevel.addFreshEntity(proj);
    }
}

// Projétil Customizado
public class ExemploProjectileEntity extends ThrowableItemProjectile {
    public ExemploProjectileEntity(EntityType<? extends ExemploProjectileEntity> type, Level level) {
        super(type, level);
    }

    public ExemploProjectileEntity(Level level, LivingEntity shooter) {
        super(CaosArcanaEntities.EXEMPLO_PROJECTILE, shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB; // Item de fallback visual
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 4.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1), this.getOwner());
        }
        if (!this.level().isClientSide) this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) this.discard();
    }
}
```

---

### 4. Chefe com Barra de Vida (`ServerBossEvent`) e Múltiplas Fases
Padrão de chefes para biomas e masmorras do Caos Arcana.

```java
public final class ExemploBossEntity extends PathfinderMob {
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.caosarcana.exemplo_boss"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    private int phase = 1;

    public ExemploBossEntity(EntityType<? extends ExemploBossEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel) {
            float healthFraction = this.getHealth() / this.getMaxHealth();
            this.bossEvent.setProgress(healthFraction);

            // Mudança de fase telegrafada
            if (healthFraction <= 0.5F && this.phase == 1) {
                this.phase = 2;
                this.bossEvent.setColor(BossEvent.BossBarColor.RED);
                // aplicar buffs de fase de fúria
            }
        }
    }
}
```

---

## 🎨 Pipeline de Modelos e Texturas (Python PIL)

Ao criar um novo mob, gere texturas pixel-art fieis à paleta sombria do Caos Arcana usando o script Python integrado:

```python
# Crie um script temporário e execute via:
# & "C:\Users\Matheus\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" script.py

from PIL import Image
import os

width, height = 128, 128
img = Image.new("RGBA", (width, height), (0, 0, 0, 0))

# Pinte as regiões conforme o layout do EntityModel (UV mapping)
# Exemplo de preenchimento de caixa (x0, y0, x1, y1):
def fill_box(im, x0, y0, x1, y1, color):
    for x in range(x0, x1):
        for y in range(y0, y1):
            im.putpixel((x, y), color)

# Preencher camadas...
out_path = r"C:\Users\Matheus\Documents\Codex\2026-10-26\https-chatgpt-com-codex-desktop-auth\modpacknovo\caos-arcana-mod\src\main\resources\assets\caosarcana\textures\entity\floresta_negra\nome_do_mob.png"
os.makedirs(os.path.dirname(out_path), exist_ok=True)
img.save(out_path)
print("Textura gerada com sucesso:", out_path)
```

---

## 📋 Checklist de Registro de Nova Entidade

Para que uma entidade exista e funcione no jogo sem crash:
1. **Classe Java da Entidade:** Criada em `br.com.caosarcana.common.entity.<Nome>Entity.java`.
2. **Registro de EntityType:** Adicionar em `CaosArcanaEntities.java`:
   ```java
   public static final EntityType<NomeEntity> NOME = Registry.register(
       BuiltInRegistries.ENTITY_TYPE,
       CaosArcana.id("nome"),
       EntityType.Builder.of(NomeEntity::new, MobCategory.MONSTER)
           .sized(largura, altura)
           .clientTrackingRange(8)
           .build("nome")
   );
   ```
3. **Registro de Atributos:** Adicionar no método `register()` de `CaosArcanaEntities.java`:
   ```java
   FabricDefaultAttributeRegistry.register(NOME, NomeEntity.createAttributes());
   ```
4. **Modelo do Cliente:** Criar em `client/model/<Nome>Model.java` com `ModelLayerLocation`.
5. **Renderer do Cliente:** Criar em `client/renderer/<Nome>Renderer.java`.
6. **Registro no Cliente:** Adicionar em `CaosArcanaClient.java`:
   ```java
   EntityModelLayerRegistry.registerModelLayer(NomeModel.LAYER_LOCATION, NomeModel::createBodyLayer);
   EntityRendererRegistry.register(CaosArcanaEntities.NOME, NomeRenderer::new);
   ```
7. **Localização:** Adicionar strings em `assets/caosarcana/lang/pt_br.json` e `en_us.json`:
   ```json
   "entity.caosarcana.nome": "Nome em Português"
   ```
8. **Compilação e Deploy:** `assemble` -> Copiar JAR para pasta mods -> Testar no jogo com `/summon caosarcana:nome ~ ~ ~`.
9. **Git:** `git pull` -> `git add` -> `git commit` -> `git push`.

---

## 🚫 Regras Críticas e Restrições Invioláveis

1. **NÃO MEXER NA ZONA DE FEITIÇOS:** `content/spells/`, `PlayerRPGMixin.java`, `IRPGPlayerData.java`, `ModMessages.java`.
2. **NÃO QUEBRAR O BUILD:** Nunca commitar sem `.\gradlew.bat assemble` resultar em `BUILD SUCCESSFUL`.
3. **RESISTÊNCIA A FOGO:** Mobs da Floresta Negra **NÃO herdam** de `TerrasDaCinzaMob` (resistência a fogo é exclusiva das Terras da Cinza).
4. **TAGS DE DANO EM 1.20.1:** Use `DamageTypeTags.IS_FALL`, nunca `DamageTypeTags.FALL` (não existe no 1.20.1).
5. **TELEGRAFIA VISUAL:** Toda habilidade poderosa (salto, sopro, invocação) DEVE possuir windup (telegrafia com partículas ou pausa) para dar oportunidade de reação ao jogador.
6. **FLUXO GIT INCONDICIONAL:** Sempre execute `git pull` antes de alterar arquivos e `git push` após cada commit.
