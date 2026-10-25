package atlantis.combat.behaviors;

import atlantis.game.player.Enemy;
import atlantis.information.tech.SpellCoordinator;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import bwapi.TechType;

/**
 * High Templar storm condensed clumps (NEXT #198, port of the old
 * UsePsionicStorm veryCondensed case): with storm energy and a clump
 * in range, the storm goes on the densest point of the clump instead
 * of walking the Templar into the fight. The Templar never storms
 * twice into the same water (coordinator dedupe) or onto an incoming
 * storm.
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
    private static final double STORM_SPACING = 3;

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
     * The center of the densest enemy clump in range, or null when no
     * clump clears the bar, the water is taken, or a storm is already
     * falling there.
     */
    static APosition stormAim(AUnit unit) {
        AUnit densest = densestEnemy(unit);
        if (densest == null) return null;

        APosition aim = densest.friendsNear()
            .groundUnits().combatUnits()
            .inRadius(STORM_RADIUS, densest)
            .center();
        if (aim == null) return null;
        if (!SpellCoordinator.noOtherSpellAssignedHere(aim, TechType.Psionic_Storm)) return null;
        if (densest.isUnderStorm()) return null;

        return aim;
    }

    /** The in-range enemy with the most enemies in storm radius of it. */
    static AUnit densestEnemy(AUnit unit) {
        Selection candidates = unit.enemiesNear().realUnits().inRadius(SEARCH_RADIUS, unit);
        if (candidates == null || candidates.isEmpty()) return null;

        AUnit best = null;
        int bestCount = minClump(unit) - 1;
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
