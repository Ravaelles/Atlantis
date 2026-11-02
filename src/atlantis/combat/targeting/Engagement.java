package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.We;

/**
 * Engagement primitives for the target ladder (NEXT #184): what counts
 * as a valid target and who is nearest engageable. Moved verbatim out of
 * the legacy squad-targeting module so the four delegating rules read
 * ladder code, not legacy code. Shared by the rules and the fight loop.
 */
public final class Engagement {

    /**
     * Damage window naming an outranged shooter (NEXT #186 B-slice): hp
     * that dropped inside this many battle frames still counts as under
     * fire, so a shelling answered a minute ago does not keep the line
     * stepping.
     */
    public static final int OUTRANGED_DAMAGE_WINDOW_FRAMES = 60;
    /**
     * Reach past the shooter's max range that still counts as its
     * envelope (splash + tile rounding, not a second opinion on range).
     */
    public static final double OUTRANGED_REACH_TILES = 1.0;

    /**
     * The shooter shelling past our reply range (NEXT #186 B-slice):
     * we are taking damage, this foe can hit us from where it stands,
     * and we cannot hit back from here. The kite envelope ignores
     * shooters past it, so without this the line reads no threat and
     * donates - Guardians halt at their own max range and shell from
     * past Dragoon reply range (measured sim: both guns stand through
     * the shelling and die with the wall). Nearest one wins; null when
     * nothing outranges us or we are not under fire.
     */
    public static AUnit nearestOutrangingShooter(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;
        if (!atlantis.battle.DuelMemory.damagedLessThanAgo(unit, OUTRANGED_DAMAGE_WINDOW_FRAMES)) return null;

        atlantis.units.select.Selection enemies = unit.enemiesNear();
        if (enemies == null) return null;

        AUnit best = null;
        double bestDist = Double.MAX_VALUE;
        for (AUnit enemy : enemies.list()) {
            if (enemy == null || !enemy.isAlive()) continue;
            if (!enemy.hasPosition()) continue;
            if (!enemy.isDetected()) continue;
            if (!isOutrangingShooter(unit, enemy)) continue;
            double dist = unit.distTo(enemy);
            if (dist < bestDist) {
                best = enemy;
                bestDist = dist;
            }
        }

        return best;
    }

    /**
     * Whether this foe is an outranged shooter for this unit: damage
     * inside the window, beyond our reply range, inside its envelope.
     * Guns that could never reply (no weapon for the foe) are not
     * outranged - that is a different doctrine, not this branch.
     */
    public static boolean isOutrangingShooter(AUnit unit, AUnit threat) {
        try {
            if (unit == null || threat == null) return false;
            if (!unit.hasWeaponToAttackThisUnit(threat)) return false;
            if (!atlantis.battle.DuelMemory.damagedLessThanAgo(unit, OUTRANGED_DAMAGE_WINDOW_FRAMES)) return false;
            double dist = unit.distTo(threat);
            if (dist <= unit.weaponRangeAgainst(threat)) return false;
            return dist <= unit.enemyWeaponRangeAgainstThisUnit(threat) + OUTRANGED_REACH_TILES;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Engagement() {
    }

    public static boolean isValidTarget(AUnit unit, AUnit enemy) {
        if (enemy == null) return false;
        // Positionless fogged memory is not a target (owner 2026-10-26:
        // cleared addresses must not crash or attract acquisition).
        if (!enemy.hasPosition()) return false;
        // Undetected enemies are not targets: the engine refuses the
        // order, so naming one only spams "not detected" every frame
        // (live: dragoons surrounding an unseen Observer). Weakest-first
        // would otherwise prefer exactly these - unreadable hp scans low.
        if (!enemy.isDetected()) return false;
        if (enemy.isABuilding() && !enemy.isCombatBuilding()) return false;
        if (enemy.isZergling()) return false;
        if (enemy.isWorker()) return false;
        // No hp floor: a nearly dead enemy is the BEST target, not an
        // invalid one (owner: dragoons kept shooting the healthiest
        // chewer while 8hp ones were unselectable - finishing is rule
        // (2) of the agreed #184 ladder, inverse of the old hp<=15 ban).
        // Dead units never reach here: every caller filters isAlive
        // before validity (focus scan, keepable, shared, squad-last).
        if (enemy.isDeadMan()) return false;
        if (preventForZealots(unit, enemy)) return false;

        return unit.hasWeaponToAttackThisUnit(enemy);
    }

    /**
     * Nearest enemy this unit can damage within maxDist tiles, or null.
     * Engagement, not focus: the focus exclusions above (lings, workers,
     * ranged mirrors, wounded) decide WHICH target to shoot once fighting -
     * this only decides WHETHER the march stops for a fight. Owner
     * 2026-10-19: the cluster walked past enemies to the anchor because
     * nothing ever set a target ({@code setLastTarget} had zero callers),
     * so every unit took the anchor-move branch every frame.
     */
    public static AUnit nearestEngageableEnemy(AUnit unit, double maxDist) {
        if (unit == null) return null;

        Selection enemies = unit.enemiesNear();
        if (enemies == null) return null;

        AUnit best = null;
        double bestDist = maxDist;
        for (AUnit enemy : enemies.list()) {
            if (enemy == null || !enemy.isAlive()) continue;
            if (!enemy.hasPosition()) continue;
            if (!enemy.isDetected()) continue;
            if (!unit.hasWeaponToAttackThisUnit(enemy)) continue;
            double dist = unit.distTo(enemy);
            if (dist < bestDist) {
                best = enemy;
                bestDist = dist;
            }
        }
        return best;
    }

    private static boolean preventForZealots(AUnit unit, AUnit enemy) {
        if (!We.protoss() || !unit.isZealot()) return false;

        if (unit.distTo(enemy) > 1.3) return false;
//        if (preventOvercrowdedZealots(unit, enemy)) return true;
        return preventZealotsTargetingRanged(unit, enemy);
    }

    private static boolean preventOvercrowdedZealots(AUnit unit, AUnit enemy) {
        return unit.allUnitsNear().inRadius(0.5, unit).atLeast(2);
    }

    private static boolean preventZealotsTargetingRanged(AUnit unit, AUnit enemy) {
        return enemy.isRanged();
    }
}
