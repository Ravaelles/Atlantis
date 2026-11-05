package atlantis.combat.cluster;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

/**
 * Early harass override (owner 2026-11-07): a healthy first Dragoon
 * does not hold formation against - or stand off from - Zerglings and
 * Zealots it can kill. The hold gates (FormHold calm slots,
 * mission standoff) read safety, not leverage: against lone melee the
 * safe move and the lethal move coincide, so the harass vetoes the
 * hold and the fight loop does the rest.
 *
 * <p>Conservative on purpose: first Dragoons only, healthy only, lone
 * Zerglings only (one or two - a pack is not a duel, the hold and the
 * kite own it), melee intel only (any known enemy ranged unit keeps
 * the hold), and never into a home breach (defense outranks harass).
 * Zealot duels keep their hold pins (a Zealot closes and chews where
 * a Ling dies kited) - vs-Zealot harass is a later slice with its own
 * superiority rule. Ranged-duel kiting still outranks the harass -
 * the veto sits below the kite gates.</p>
 */
public final class EarlyHarass {

    /**
     * Past this many Dragoons the army fights formations, not duels.
     */
    public static final int MAX_HARASS_DRAGOONS = 2;

    /**
     * How far the harass looks for its duel (tiles). Beyond this the
     * contact belongs to the hunt, not to an override.
     */
    public static final int HARASS_SIGHT_TILES = 12;

    /**
     * Past this many visible melee foes it is a pack, not a duel - the
     * hold and the kite own packs (measured sim: two Dragoons meeting
     * five Lings must kite, never engage).
     */
    public static final int MAX_HARASS_FOES = 2;

    private EarlyHarass() {
    }

    /**
     * True when this Dragoon should meet the melee in front of it
     * instead of holding formation or standing off. The target may be
     * null (mission standoff with no acquired contact): then the
     * nearest visible duel shapes the answer.
     */
    public static boolean shouldHarass(AUnit unit, AUnit target) {
        try {
            return shouldHarassInner(unit, target);
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static boolean shouldHarassInner(AUnit unit, AUnit target) {
        if (unit == null || !unit.isAlive() || !unit.isDragoon()) return false;
        if (unit.isAttacking()) return false;
        if (Count.dragoons() > MAX_HARASS_DRAGOONS) return false;
        if (unit.hp() < unit.maxHp() * 0.9) return false;
        if (atlantis.combat.cluster.ClusterBreach.breachForcesFight()) return false;
        if (!Select.enemy().ranged().isEmpty()) return false;

        AUnit foe = target != null ? target : nearestDuel(unit);
        if (foe == null || !foe.isAlive() || !foe.hasPosition()) return false;
        if (!foe.isZergling()) return false;

        return meleeFoesNear(unit) <= MAX_HARASS_FOES;
    }

    /**
     * Visible live enemies near the Dragoon: the duel must stay a duel.
     */
    private static int meleeFoesNear(AUnit unit) {
        int n = 0;
        for (AUnit enemy : Select.enemy().list()) {
            if (enemy == null || !enemy.isAlive() || !enemy.hasPosition()) continue;
            if (!enemy.isVisibleUnitOnMap()) continue;
            if (unit.distTo(enemy) > HARASS_SIGHT_TILES) continue;
            n++;
        }

        return n;
    }

    private static AUnit nearestDuel(AUnit unit) {
        AUnit nearest = null;
        double best = HARASS_SIGHT_TILES;

        for (AUnit enemy : Select.enemy().list()) {
            if (enemy == null || !enemy.isAlive() || !enemy.hasPosition()) continue;
            if (!enemy.isVisibleUnitOnMap()) continue;
            double d = unit.distTo(enemy);
            if (d < best) {
                best = d;
                nearest = enemy;
            }
        }

        return nearest;
    }
}
