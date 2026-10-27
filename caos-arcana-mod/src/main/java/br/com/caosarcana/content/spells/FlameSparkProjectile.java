package br.com.caosarcana.content.spells;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Projétil próprio do Caos Arcana para a magia Centelha Ígnea.
 * Encerrado estritamente na Server Thread no primeiro destes eventos:
 * 1. Impacto com entidade ou bloco (comportamento herdado com descarte imediato).
 * 2. 60 ticks de tempo de voo (3 segundos).
 * 3. 40 blocos de deslocamento acumulado desde a origem do disparo.
 */
public class FlameSparkProjectile extends SmallFireball {
    // Parâmetros provisórios de balanceamento
    public static final int MAX_LIFETIME_TICKS = 60;
    public static final double MAX_DISTANCE_BLOCKS = 40.0;

    private Vec3 spawnPosition;
    private int flightTicks = 0;

    public FlameSparkProjectile(EntityType<? extends SmallFireball> entityType, Level level) {
        super(entityType, level);
        this.spawnPosition = this.position();
    }

    public FlameSparkProjectile(Level level, LivingEntity shooter, double xPower, double yPower, double zPower) {
        super(level, shooter, xPower, yPower, zPower);
        this.spawnPosition = shooter.position();
    }

    public FlameSparkProjectile(Level level, double x, double y, double z, double xPower, double yPower, double zPower) {
        super(level, x, y, z, xPower, yPower, zPower);
        this.spawnPosition = new Vec3(x, y, z);
    }

    public void setSpawnPosition(Vec3 pos) {
        this.spawnPosition = pos;
    }

    public Vec3 getSpawnPosition() {
        return this.spawnPosition;
    }

    public int getFlightTicks() {
        return this.flightTicks;
    }

    public double getDistanceFromSpawn() {
        if (this.spawnPosition == null) {
            return 0.0;
        }
        return this.position().distanceTo(this.spawnPosition);
    }

    public boolean isExpiredByTime() {
        return this.flightTicks >= MAX_LIFETIME_TICKS;
    }

    public boolean isExpiredByDistance() {
        return getDistanceFromSpawn() >= MAX_DISTANCE_BLOCKS;
    }

    @Override
    public void tick() {
        super.tick();

        // Controle e encerramento estrito pelo lado do servidor
        if (!this.level().isClientSide()) {
            this.flightTicks++;

            if (this.spawnPosition == null) {
                this.spawnPosition = this.position();
            }

            // Encerrar no primeiro dos eventos: 60 ticks ou 40 blocos
            if (isExpiredByTime() || isExpiredByDistance()) {
                // Efeito sutil de dissipação ao expirar (sem dano, sem fogo e sem explosão)
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
                            this.getX(), this.getY(), this.getZ(),
                            6, 0.1, 0.1, 0.1, 0.02
                    );
                }
                // Remoção limpa; o servidor automaticamente envia pacote de remoção para os clientes
                this.discard();
            }
        }
    }
}
