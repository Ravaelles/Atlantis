package tests.acceptance;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.conditions.EnemiesTooClose;
import atlantis.production.constructions.position.conditions.OtherConstructionTooClose;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The two conditions that ask about <b>other things on the map</b> rather than
 * about terrain: a pending construction of ours, and an enemy unit.
 *
 * <p>
 * Both were untested. They are also the two that can refuse a perfectly good
 * tile
 * for a reason that has nothing to do with the tile, so a bug here is invisible
 * from the placement side - the search simply keeps rejecting candidates.
 * </p>
 */
public class EnemyProximityConditionsTest extends WorldStubForTests {

    private static final AUnitType PYLON = AUnitType.Protoss_Pylon;
    private static final AUnitType GATEWAY = AUnitType.Protoss_Gateway;

    private static FakeUnit main;
    private static FakeUnit enemy;

    /**
     * A pending construction at a tile, created where the game exists.
     */
    private static Construction pending(AUnitType type, int tx, int ty) {
        Construction construction = new Construction(type);
        construction.setPositionToBuild(APosition.create(tx, ty));

        return construction;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void resetStatusAndQueue() {
        AbstractPositionFinder._STATUS = "Init";
        ConstructionRequests.constructions.clear();
    }

    @AfterEach
    public void tearDown() {
        ConstructionRequests.constructions.clear();
        EnemyUnits.clearCache();
    }

    // ---------------------------------------------------------------------------
    // OtherConstructionTooClose: our own pending buildings
    // ---------------------------------------------------------------------------

    /**
     * A base is exempt - it defines its own surroundings.
     */
    @Test
    public void aBaseIsExemptFromTheOtherConstructionRule() {
        world(1, fakeOurs(main = fake(AUnitType.Protoss_Nexus, 50, 50)), fakeEnemies(), () -> {
            assertFalse(
                OtherConstructionTooClose.isOtherConstructionTooClose(
                    main, AUnitType.Protoss_Nexus, APosition.create(50, 50)),
                "a base is not judged against other constructions");
        });
    }

    /**
     * A pending construction already standing on the tile must make it unavailable
     * -
     * that is the rule's whole purpose, and without it two buildings would be
     * planned onto one tile.
     */
    @Test
    public void aTileAlreadyClaimedByAPendingConstructionIsRefused() {
        world(1, fakeOurs(
            main = fake(AUnitType.Protoss_Nexus, 50, 50),
            fake(AUnitType.Protoss_Probe, 51, 51)), fakeEnemies(), () -> {
            Construction pending = new Construction(PYLON);
            pending.setPositionToBuild(APosition.create(55, 55));
            ConstructionRequests.constructions.add(pending);

            assertTrue(
                OtherConstructionTooClose.isOtherConstructionTooClose(
                    main, GATEWAY, APosition.create(55, 55)),
                "the tile a pending Pylon already claims must be refused");
        });
    }

    /**
     * A tile far from every pending construction is accepted.
     */
    @Test
    public void aTileFarFromPendingConstructionsIsAccepted() {
        world(1, fakeOurs(
            main = fake(AUnitType.Protoss_Nexus, 50, 50),
            fake(AUnitType.Protoss_Probe, 51, 51)), fakeEnemies(), () -> {
            Construction pending = new Construction(PYLON);
            pending.setPositionToBuild(APosition.create(55, 55));
            ConstructionRequests.constructions.add(pending);

            assertFalse(
                OtherConstructionTooClose.isOtherConstructionTooClose(
                    main, GATEWAY, APosition.create(80, 80)),
                "a tile well clear of the pending Pylon is accepted");
        });
    }

    // ---------------------------------------------------------------------------
    // EnemiesTooClose / EnemyTooCloseToUnstartedConstruction
    // ---------------------------------------------------------------------------

    /**
     * With no enemy on the map, nothing is too close to an enemy.
     */
    @Test
    public void withNoEnemiesNothingIsTooClose() {
        world(1, fakeOurs(
            main = fake(AUnitType.Protoss_Nexus, 50, 50),
            fake(AUnitType.Protoss_Probe, 51, 51)), fakeEnemies(), () -> {
            assertFalse(
                EnemiesTooClose.check(main, PYLON, APosition.create(55, 55)),
                "no enemy means no enemy proximity refusal");
        });
    }

    /**
     * An enemy combat unit sitting on the candidate tile refuses it, and the reason
     * names the enemy - the status string is what a later reader has to work from,
     * so it is asserted rather than assumed.
     */
    @Test
    public void anEnemyCombatUnitOnTheTileRefusesIt() {
        world(1, fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 50, 50),
                fake(AUnitType.Protoss_Probe, 51, 51)),
            fakeEnemies(
                enemy = fake(AUnitType.Zerg_Zergling, 56, 55)),
            () -> {
                assertTrue(
                    EnemiesTooClose.check(main, PYLON, APosition.create(55, 55)),
                    "a Zergling next to the candidate tile must refuse it");
                assertTrue(
                    AbstractPositionFinder._STATUS.contains("TooCloseEnemy"),
                    "the refusal must be attributed to an enemy, got: "
                        + AbstractPositionFinder._STATUS);
            });
    }

    /**
     * The distance gate is 8 tiles beyond the enemy's weapon range: an enemy far
     * enough away must not block placement, or the bot could never build anywhere
     * on
     * a map with a distant opponent.
     */
    @Test
    public void aDistantEnemyDoesNotRefuseATile() {
        world(1, fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 50, 50),
                fake(AUnitType.Protoss_Probe, 51, 51)),
            fakeEnemies(
                enemy = fake(AUnitType.Zerg_Zergling, 120, 120)),
            () -> {
                assertFalse(
                    EnemiesTooClose.check(main, PYLON, APosition.create(55, 55)),
                    "an enemy far across the map must not block a build position");
            });
    }
}
