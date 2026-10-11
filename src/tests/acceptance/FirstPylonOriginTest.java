package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.PylonPosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Where the search for the FIRST Pylon starts looking.
 *
 * <p>
 * This is the defect that cost the 2026-10-10 session, so it gets a test of its
 * own.
 * {@code nearToPositionForFirstPylon()} used to compute
 * {@code geyser.translateTilesTowards(mineralsCenter, 50)}, which on TauCross
 * produced a
 * point 36 tiles off the map; {@code ensureWithinBounds()} then answered
 * {@code null},
 * the {@code if (centerOfResources != null)} guard skipped the step meant to
 * move the
 * origin off the base, and the method returned <b>the Nexus position</b>. The
 * search then
 * started on top of the Nexus, every low-radius candidate was legitimately
 * "occupied",
 * and no Pylon was ever placed.
 * </p>
 *
 * <p>
 * The assertion that would have caught it is not "the origin is 7 tiles away" -
 * it is the
 * weaker, sturdier property that the search must not start <b>on the base it is
 * trying to
 * build beside</b>. That is the invariant; the exact distance is tuning.
 * </p>
 */
public class FirstPylonOriginTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * The origin is never null in a live game: a null {@code nearTo} reaches the
     * finders
     * as "near = null", which made every build position fail (see
     * {@code Select.main()}'s
     * comment on the same failure).
     */
    @Test
    public void theFirstPylonSearchOriginIsNeverNull() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 50, 50);

        world(1, fakeOurs(nexus), fakeEnemies(), () -> {
            APosition origin = PylonPosition.nearToPositionForFirstPylon();

            assertNotNull(origin, "a null origin makes the whole placement search fail");
        });
    }

    /**
     * <b>The regression this class exists for.</b> The origin must not collapse
     * onto the
     * Nexus: a search that starts there spends its first rings on tiles the engine
     * will
     * always refuse, because the base itself stands on them.
     */
    @Test
    public void theSearchDoesNotStartOnTopOfTheBaseItBuildsBeside() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 50, 50);

        world(1, fakeOurs(nexus), fakeEnemies(), () -> {
            APosition origin = PylonPosition.nearToPositionForFirstPylon();

            assertNotNull(origin, "precondition");

            assertTrue(
                nexus.distTo(origin) > 1.0,
                    "the first-Pylon search must not start on the Nexus - measured 2026-10-10: "
                            + "it returned distBase=0.84, and every candidate was therefore an "
                            + "occupied tile, so no Pylon was ever built");
        });
    }

    /**
     * The origin must be inside the map. The old code produced a point 36 tiles off
     * it,
     * which is what made {@code ensureWithinBounds()} return null and collapse the
     * whole
     * computation back onto the base.
     */
    @Test
    public void theFirstPylonSearchOriginIsInsideTheMap() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 50, 50);

        world(1, fakeOurs(nexus), fakeEnemies(), () -> {
            APosition origin = PylonPosition.nearToPositionForFirstPylon();

            assertNotNull(origin, "precondition");

            assertTrue(
                    !origin.isOutOfBounds(),
                    "an origin outside the map cannot be searched from - the old chain hit "
                            + "exactly this (raw=[-36,88]) and silently degraded to the base");
        });
    }

    /**
     * A base near a map edge is the case that made a blind "towards the resources"
     * step
     * fail: the resource line points at the very edge the base is near, so that
     * direction
     * leads into the boundary margin. The origin must still land somewhere usable.
     */
    @Test
    public void theOriginIsSaneForABaseInAMapCorner() {
        FakeUnit cornerNexus = fake(AUnitType.Protoss_Nexus, 2, 2);

        world(1, fakeOurs(cornerNexus), fakeEnemies(), () -> {
            APosition origin = PylonPosition.nearToPositionForFirstPylon();

            assertNotNull(origin, "a corner base must not break the origin computation");
            assertTrue(!origin.isOutOfBounds(), "the origin must stay on the map");
        });
    }

    /**
     * The degradation case this method got wrong <b>twice</b>. When the map offers no
     * separate resource point to aim at, {@code ABaseLocation.mineralsCenter} returns
     * <b>the base itself</b> - and "translate towards yourself" moves by exactly zero,
     * which puts the origin back on the Nexus and recreates the original failure one level
     * deeper. Measured 2026-10-10 in this stub: mineralsCenter = the Nexus, origin = the
     * Nexus, dist = 0.0.
     *
     * <p>
     * A direction must be a <b>different point</b>, or it must be ignored.
     * </p>
     */
    @Test
    public void aResourceCentreEqualToTheBaseIsNotUsedAsADirection() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 50, 50);

        world(1, fakeOurs(nexus), fakeEnemies(), () -> {
            APosition origin = PylonPosition.nearToPositionForFirstPylon();

            assertNotNull(origin, "precondition");

            assertTrue(
                nexus.distTo(origin) > 1.0,
                "no minerals on the map makes mineralsCenter return the base; that must "
                    + "degrade to the map-centre direction, not to a zero-length step"
            );
        });
    }
}
