package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.formation.SharedEscape;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Pins the shared escape direction (formation Phase A): one direction
 * per cluster per ~10 frames, every follower rallies along it from its
 * own position, and the destination always leads away from the shared
 * threat. Duels and pairs keep the per-unit sampling - sharing answers
 * null for them.
 */
@Tag("combat")
public class SharedEscapeTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        BattleCommander.clearForTests();
    }

    @Test
    public void pairKeepsPerUnitSampling() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 12, 10);
        FakeUnit threat = fake(AUnitType.Protoss_Zealot, 16, 10);

        world(1, fakeOurs(a, b), fakeEnemies(threat), () -> {
            assertNull(SharedEscape.rallyFor(b, a, Select.our(), 6.0, 15.0),
                    "two units share nothing - the frozen per-unit escape owns them");
        });
    }

    @Test
    public void followersRallyAlongOneDirectionAwayFromTheThreat() {
        FakeUnit leader = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit mid = fake(AUnitType.Protoss_Dragoon, 12, 10);
        FakeUnit tail = fake(AUnitType.Protoss_Dragoon, 14, 10);
        FakeUnit threat = fake(AUnitType.Protoss_Zealot, 20, 10);

        world(1, fakeOurs(leader, mid, tail), fakeEnemies(threat), () -> {
            APosition rallyMid = SharedEscape.rallyFor(mid, leader, Select.our(), 6.0, 15.0);
            APosition rallyTail = SharedEscape.rallyFor(tail, leader, Select.our(), 6.0, 15.0);

            assertNotNull(rallyMid);
            assertNotNull(rallyTail);

            assertTrue(rallyMid.distTo(threat) > mid.distTo(threat),
                    "the shared rally leads away from the threat, not toward it");

            double dirMidX = rallyMid.tx() - mid.tx();
            double dirTailX = rallyTail.tx() - tail.tx();
            assertTrue(dirMidX < 0 && dirTailX < 0,
                    "threat sits east, both followers rally west along the one direction");

            assertEquals(
                Math.signum(rallyMid.ty() - mid.ty()),
                Math.signum(rallyTail.ty() - tail.ty()),
                "followers share the lateral component too - one direction, not two");
        });
    }

    @Test
    public void directionHoldsAcrossBuckets() {
        FakeUnit leader = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit mid = fake(AUnitType.Protoss_Dragoon, 12, 10);
        FakeUnit tail = fake(AUnitType.Protoss_Dragoon, 14, 10);
        FakeUnit threat = fake(AUnitType.Protoss_Zealot, 20, 10);

        final APosition[] seen = new APosition[40];

        world(25, fakeOurs(leader, mid, tail), fakeEnemies(threat), () -> {
            int now = atlantis.game.A.now();
            seen[now] = SharedEscape.rallyFor(mid, leader, Select.our(), 6.0, 15.0);
        });

        assertNotNull(seen[3]);
        assertNotNull(seen[14]);
        assertEquals(seen[3], seen[14],
                "static threat and leader mean a static direction - no jitter across the 10f cadence");
    }
}
