package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * The harness empties squad rosters between tests (NEXT #188 follow-up:
 * a BlockingForceSimTest run died with "FakeUnit.position() is null"
 * inside a selection because RetreatSplitChainTest's Alpha roster
 * survived its cleanup - cleanUp() nulls every fake's position, but
 * nothing emptied the roster holding them).
 */
@Tag("combat")
public class AlphaRosterHygieneTest extends WorldStubForTests {

    @Test
    public void rosterDoesNotSurviveClearAll() {
        FakeUnit a = fake(AUnitType.Protoss_Zealot, 30, 30);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 32, 30);

        world(3, fakeOurs(a, b), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() != 1) return;
            assertTrue(Alpha.get().isEmpty(), "roster starts empty in a fresh world");

            Alpha.gather();
            assertEquals(2, Alpha.get().size());

            tests.unit.helpers.ClearAllCaches.clearAll();
            assertTrue(Alpha.get().isEmpty(), "roster emptied, no phantom units for the next test");
        });
    }
}
