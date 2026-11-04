package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void joiningSetsBothSides() {
        FakeUnit a = fake(AUnitType.Protoss_Zealot, 30, 30);

        world(3, fakeOurs(a), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() != 1) return;

            Alpha.gather();

            assertTrue(a.squad() == Alpha.get(),
                "the back-pointer is part of joining: a bare roster entry "
                    + "keeps unit.squad() null, which reads as a bug in every "
                    + "squad reader while the unit already follows Alpha");
        });
    }

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
