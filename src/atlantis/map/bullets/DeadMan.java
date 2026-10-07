package atlantis.map.bullets;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.attacked_by.Bullets;
import atlantis.util.cache.Cache;
import atlantis.units.select.CacheKey;

import java.util.List;

public class DeadMan implements ClearsCache {
    private static Cache<Boolean> cache = new Cache<>();
    private static List<ABullet> bulletsAgainst;

    /**
     * Re-entrancy guard for the {@code enemiesNear()} cycle.
     *
     * <p>
     * {@code willBeDeadMan} asks {@code Bullets.against}, which asks
     * {@code PendingAttacksAgainstEnemyUnit.against}, which calls
     * {@code enemy.enemiesNear()} - and {@code enemiesNear()} filters through
     * {@code isDeadMan}. That is a cycle:
     * </p>
     *
     * <pre>
     * isDeadMan -> Bullets.against -> PendingAttacks.against
     *            -> enemiesNear -> isDeadMan -> ...
     * </pre>
     *
     * <p>
     * Normally the {@link Cache} answers first and the cycle never completes a
     * full turn. When it does not (a fresh unit, an empty cache - measured
     * 2026-10-07 with a {@code StackOverflowError} from
     * {@code WorkerDefenceFightCombatUnits.applies}), the stack unwinds into a
     * crash. While this thread is already inside the calculation the answer is
     * "not a dead man": it is the conservative choice (we do not write a unit
     * off), and it is the only one that terminates.
     * </p>
     */
    private static final ThreadLocal<Boolean> CALCULATING = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public static boolean isDeadMan(AUnit unit) {
        if (Boolean.TRUE.equals(CALCULATING.get())) return false;

        CALCULATING.set(true);
        try {
            return cache.get(
                CacheKey.toKey(unit),
                0,
                () -> willBeDeadMan(unit)
            );
        } finally {
            CALCULATING.set(false);
        }
    }

    private static boolean willBeDeadMan(AUnit unit) {
        if (unit.isNeutral()) return false;
        if (unit.isFoggedUnitWithKnownPosition()) return false;

        bulletsAgainst = Bullets.against(unit);
        if (bulletsAgainst.isEmpty()) return false;

        int willGetDamage = damageWithAllPendingBullets(unit);
        int hasHp = unit.hp() + healthBonus(unit);
        boolean isDeadManWalking = willGetDamage >= hasHp;

//        if (isDeadManWalking) {
//            System.err.println(
//                "@ " + A.now() + " - " + unit.typeWithUnitId()
//                    + " - HP: " + hasHp
//                    + " / Damage:" + willGetDamage
//            );
//        }

        return isDeadManWalking;
    }

    private static int healthBonus(AUnit unit) {
        if (unit.isTerran()) return 0;
        return 1;
    }

    private static int damageWithAllPendingBullets(AUnit unit) {
        int damage = 0;
        for (ABullet bullet : bulletsAgainst) {
            damage += BulletDamageAgainst.forBullet(bullet);
        }
//        System.err.println("bulletsAgainst = " + bulletsAgainst.size() + " / damage = " + damage);
        return damage;
    }
}
