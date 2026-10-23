package atlantis.battle.targeting;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;

/**
 * The chewed-melee rule (owner, from the Steamhammer loss: our Zealot#177
 * died surrounded by Zerglings with no stable target for 5 seconds while
 * the orders chased distant Zerglings #164/#173/#153). A melee unit with
 * its shot ready (cooldown &lt;= 5) ALWAYS hits the weakest enemy IN
 * RANGE instead of chasing far targets or marching.
 *
 * <p>Why a separate rule next to the focus chain: every focus path
 * ({@code isValidTarget}) bans Zerglings and workers, so against a ling
 * flood keepable drops the chewer every frame and the in-range scan
 * skips every adjacent enemy - the fallbacks then hand far squad and
 * shared targets. A melee unit in contact does no pack focus, so the
 * bans do not apply here; ranged units and all sharing paths keep
 * theirs (SpreadFire pack discipline untouched).</p>
 */
public final class MeleeChewerTarget {

    private MeleeChewerTarget() {
    }

    /** Shot ready now: 5 frames or less on the weapon. */
    public static final int READY_COOLDOWN_FRAMES = 5;

    /**
     * Melee hitting reach in tiles, engine-measured (claws reach 15px;
     * tile truncation rounds melee bodies to zero, so the nominal
     * weapon-range table reads 0 for Zealots - the same 0.5 line
     * {@code Kite} uses for contact). Raw distance, not the range
     * table: the table cannot express contact.
     */
    public static final double MELEE_HIT_TILES = 0.5;

    /**
     * Weakest damageable enemy inside this melee unit's weapon range, or
     * null when the rule does not apply (not melee, shot not ready, or
     * nothing in reach). No type bans by design - see the class docs.
     */
    public static AUnit pick(AUnit unit) {
        if (unit == null || !unit.isAlive() || !unit.isMelee()) return null;
        if (unit.cooldown() > READY_COOLDOWN_FRAMES) return null;

        Selection enemies;
        try {
            enemies = unit.enemiesNear();
        }
        catch (RuntimeException ignored) {
            return null;
        }
        if (enemies == null) return null;

        AUnit best = null;
        int bestHp = Integer.MAX_VALUE;
        double bestDist = Double.MAX_VALUE;
        for (AUnit enemy : enemies.list()) {
            if (enemy == null || !enemy.isAlive()) continue;
            if (!enemy.hasPosition()) continue;
            if (!unit.hasWeaponToAttackThisUnit(enemy)) continue;
            if (unit.distTo(enemy) > MELEE_HIT_TILES) continue;

            int hp = enemy.hp() + enemy.shields();
            double dist = unit.distTo(enemy);
            if (hp < bestHp || (hp == bestHp && dist < bestDist)) {
                bestHp = hp;
                bestDist = dist;
                best = enemy;
            }
        }

        return best;
    }
}
