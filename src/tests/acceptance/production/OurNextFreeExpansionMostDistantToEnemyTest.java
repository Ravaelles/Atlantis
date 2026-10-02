package tests.acceptance.production;

import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.base.OurNextFreeExpansionMostDistantToEnemy;
import atlantis.units.AUnitType;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Picks the free expansion base that scores best on "far from our main, far from
 * the enemy" ({@code -1.5 * distance to main + distance to the nearest enemy
 * building}).
 *
 * <p>This file used to sit in the production tree
 * ({@code atlantis/production/constructions/position/base/}) with its single
 * test commented out, so it asserted nothing.</p>
 *
 * <p>The world has our main at (7, 44) and a second Nexus at (7, 80), which
 * occupies the base location at (7, 78) and takes it out of the running. What is
 * left is the natural at (14, 13) and the upper-left base at (23, 100), and the
 * two tests below differ only in where the Zerg hatchery is - which is the whole
 * point: the answer follows the enemy.</p>
 */
public class OurNextFreeExpansionMostDistantToEnemyTest extends WorldStubForTests {

    /**
     * Zerg hatchery in the top right corner, so the natural at (14, 13) is both
     * the freest base and the one furthest from the enemy. It wins.
     */
    @Test
    public void takesTheNaturalWhenTheEnemyBaseIsFarAway() {
        options = Options.create().set("supplyUsed", 18);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),  // main
            fake(AUnitType.Protoss_Nexus, 7, 80)   // blocks the (7, 78) base location
        ), fakeEnemies(), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Zergling, 20, 10));
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Hatchery, 90, 10));

            APosition location = OurNextFreeExpansionMostDistantToEnemy.find();

            assertNotNull(location, "there is a free base to expand to");
            assertEquals(14, location.tx());
            assertEquals(13, location.ty());
        });
    }

    /**
     * The same world with the Zerg base sitting on top of the natural. The
     * finder gives it up and takes (23, 100) instead - so the choice is driven
     * by the enemy, not by a hardcoded coordinate.
     */
    @Test
    public void movesAwayFromAnEnemyBaseOnTheNatural() {
        options = Options.create().set("supplyUsed", 18);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Nexus, 7, 80)
        ), fakeEnemies(), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Zergling, 20, 10));
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Hatchery, 14, 13));

            APosition location = OurNextFreeExpansionMostDistantToEnemy.find();

            assertNotNull(location, "there is a free base to expand to");
            assertEquals(23, location.tx(), "the natural at (14, 13) is the Zerg base's spot now");
            assertEquals(100, location.ty());
        });
    }
}