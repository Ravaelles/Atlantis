package atlantis.combat.behaviors;

import atlantis.game.player.Enemy;
import atlantis.information.tech.SpellCoordinator;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.TechType;

/**
 * High Templar storm condensed clumps and sniped hardware (NEXT #198,
 * port of the old UsePsionicStorm: veryCondensed, crucial, important,
 * almost-dead - in the legacy priority order). With storm energy the
 * storm goes on the densest point or the sniped unit instead of walking
 * the Templar into the fight. The Templar never storms twice into the
 * same water (coordinator dedupe) or onto an incoming storm.
 *
 * <p>Numbers carry legacy provenance: 75 energy, the 8.8 search and
 * 3.3 storm radii, the 3-vs-Protoss / 4-otherwise bar lowered by one
 * past 110 energy. The crucial/important snipes (Reaver, Tank) and
 * the deep-energy relaxations stay a later slice.</p>
 */
public final class CastStorm {

    private static final int STORM_ENERGY = 75;
    private static final int CHARGED_ENERGY = 110;
    private static final double SEARCH_RADIUS = 8.8;
    private static final double STORM_RADIUS = 3.3;

    /** Sniped hardware counts inside this range of the Templar. */
    private static final double CRUCIAL_RADIUS = 12;
    private static final double IMPORTANT_RADIUS = 8.9;

    /** Storming into more of ours than this wastes the line. */
    private static final int OURS_NEAR_MAX = 2;

    /** An important target needs at least this many enemies around it. */
    private static final int IMPORTANT_PACK_MIN = 2;

    /** Nearly dead Templar storms whatever it can reach. */
    private static final int ALMOST_DEAD_HP = 31;

    private CastStorm() {
    }

    /** Only charged High Templar with a stormable clump in range. */
    public static boolean applies(AUnit unit) {
        if (unit == null || !unit.isAlive()) return false;
        if (!unit.isHighTemplar()) return false;
        if (!unit.energy(STORM_ENERGY)) return false;

        return stormAim(unit) != null;
    }

    /**
     * Storm the densest point of the clump. True when an order went out.
     */
    public static boolean act(AUnit unit) {
        APosition aim = applies(unit) ? stormAim(unit) : null;
        if (aim == null) return false;

        return unit.useTech(TechType.Psionic_Storm, aim);
    }

    /**
     * The storm aim, first shape that fires (legacy order): the densest
     * clump, a crucial sniped unit, an important one with a pack, or -
     * for a nearly dead Templar - whatever it can reach. Null when no
     * shape fires, the water is taken, or a storm is already falling.
     */
    static APosition stormAim(AUnit unit) {
        APosition aim = condensedAim(unit, false);
        if (aim == null) aim = crucialAim(unit);
        if (aim == null) aim = importantAim(unit);
        if (aim == null && unit.hp() <= ALMOST_DEAD_HP) aim = condensedAim(unit, true);

        return aim;
    }

    /**
     * The center of the densest enemy clump in range, or null when no
     * clump clears the bar. Forced (a dying Templar) storms whatever
     * is in reach.
     */
    static APosition condensedAim(AUnit unit, boolean forced) {
        AUnit densest = densestEnemy(unit, forced);
        if (densest == null) return null;

        return aimedAt(densest);
    }

    /** A Reaver or sieged Tank with few of ours on it: storm it. */
    static APosition crucialAim(AUnit unit) {
        AUnit crucial = firstSniped(unit, CRUCIAL_RADIUS,
            AUnitType.Protoss_Reaver, AUnitType.Terran_Siege_Tank_Siege_Mode);
        if (crucial == null) return null;

        return aimedAt(crucial);
    }

    /** A Carrier, Reaver, Vessel, Tank or Defiler with a pack and few of ours on it. */
    static APosition importantAim(AUnit unit) {
        AUnit important = firstSniped(unit, IMPORTANT_RADIUS,
            AUnitType.Protoss_Carrier, AUnitType.Protoss_Reaver,
            AUnitType.Terran_Science_Vessel, AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Zerg_Defiler);
        if (important == null) return null;
        if (snipeEnemies(unit).inRadius(STORM_RADIUS, important).count() < IMPORTANT_PACK_MIN) return null;

        return aimedAt(important);
    }

    /** The first sniped type in range with few of ours on it, or null. */
    static AUnit firstSniped(AUnit unit, double radius, AUnitType... types) {
        Selection candidates = snipeEnemies(unit);
        if (candidates == null) return null;

        for (AUnit enemy : candidates.ofType(types).inRadius(radius, unit).list()) {
            if (Select.ourRealUnits().inRadius(2, enemy).atMost(OURS_NEAR_MAX)) return enemy;
        }

        return null;
    }

    /** Snipe targets: real enemies near the Templar, never Overlords (those are the Corsairs'). */
    static Selection snipeEnemies(AUnit unit) {
        Selection enemies = unit.enemiesNear().realUnits();
        if (enemies == null || enemies.isEmpty()) return enemies;

        Selection overlords = enemies.overlords();
        if (overlords == null || overlords.isEmpty()) return enemies;

        return enemies.exclude(overlords.list());
    }

    /**
     * The stormable center around this enemy, or null when the water is
     * taken or a storm is already falling there.
     */
    static APosition aimedAt(AUnit enemy) {
        APosition aim = enemy.friendsNear()
            .groundUnits().combatUnits()
            .inRadius(STORM_RADIUS, enemy)
            .center();
        if (aim == null) return null;
        if (!SpellCoordinator.noOtherSpellAssignedHere(aim, TechType.Psionic_Storm)) return null;
        if (enemy.isUnderStorm()) return null;

        return aim;
    }

    /** The in-range enemy with the most enemies in storm radius of it. */
    static AUnit densestEnemy(AUnit unit, boolean forced) {
        Selection candidates = unit.enemiesNear().realUnits().inRadius(SEARCH_RADIUS, unit);
        if (candidates == null || candidates.isEmpty()) return null;

        AUnit best = null;
        int bestCount = forced ? 0 : minClump(unit) - 1;
        for (AUnit enemy : candidates.list()) {
            if (enemy.isUnderStorm()) continue;
            int count = candidates.inRadius(STORM_RADIUS, enemy).count();
            if (count > bestCount) {
                best = enemy;
                bestCount = count;
            }
        }

        return best;
    }

    /**
     * The legacy bar: 3 vs Protoss, 4 otherwise, lowered by one past
     * 110 energy. Strictly greater wins, so the bar itself never storms.
     */
    static int minClump(AUnit unit) {
        int base = Enemy.protoss() ? 3 : 4;

        return unit.energy(CHARGED_ENERGY) ? base - 1 : base;
    }
}
