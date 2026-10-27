package tests.unit;

import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.strategy.ChokeStandoff;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Choke standoff depth (owner order 2026-10-25): a line forming up at a
 * choke always stands RADIUS + 6 tiles off its center, so the enemy has
 * to commit into the funnel instead of meeting us at the mouth. The
 * owner's own example is pinned verbatim - a 9-tile radius stands 15
 * tiles back - and the retreat rally answers the same depth, so one
 * doctrine, one number.
 */
@Tag("combat")
public class ChokeStandoffDistanceTest extends WorldStubForTests {

    @Test
    public void standoffIsRadiusPlusSix() {
        APosition center = APosition.create(50, 50);
        APosition ours = APosition.create(50, 90);

        APosition hold = ChokeStandoff.holdPoint(new FakeChoke(center, 18), ours);

        assertNotNull(hold, "the hold names a point");
        assertTrue(Math.abs(hold.distTo(center) - (9 + 6)) < 0.5,
            "9-tile radius stands RADIUS + 6 = 15 tiles off the center (owner example): "
                + hold.distTo(center));
        assertTrue(hold.distTo(ours) < center.distTo(ours),
            "on our side of the center, between us and it");
    }

    @Test
    public void widerChokeStandsFurtherBack() {
        APosition center = APosition.create(50, 50);
        APosition ours = APosition.create(50, 90);

        APosition narrow = ChokeStandoff.holdPoint(new FakeChoke(center, 6), ours);
        APosition wide = ChokeStandoff.holdPoint(new FakeChoke(center, 24), ours);

        assertTrue(narrow != null && wide != null, "both holds answer a point");
        assertTrue(wide.distTo(center) > narrow.distTo(center) + 5,
            "a wider opening pushes the line further back, not the same fixed depth: "
                + narrow.distTo(center) + " vs " + wide.distTo(center));
    }

    @Test
    public void retreatRallyUsesTheSameDepth() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        AChoke mainChoke = new FakeChoke(APosition.create(60, 20), 10);

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);

            world(1, fakeOurs(nexus), fakeEnemies(), () -> {
                HasPosition standoff = ClusterRally.chokeStandoffPoint();
                assertNotNull(standoff, "the stub world has a main choke to hold");

                double expected = (mainChoke.width() / 2.0) + ChokeStandoff.SPARE_TILES;
                assertTrue(Math.abs(standoff.distTo(mainChoke.center()) - expected) < 0.8,
                    "the rally stands RADIUS + 6 off the center: "
                        + standoff.distTo(mainChoke.center()) + " vs " + expected);
            });
        }
    }
}
