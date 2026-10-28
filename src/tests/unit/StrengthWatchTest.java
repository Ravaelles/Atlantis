package tests.unit;

import atlantis.combat.twins.StrengthWatch;
import atlantis.combat.twins.StrengthWatch;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * STRENGTH_WATCH rows only on contact transitions (plus the first
 * sample) - a per-frame dump would drown the trace for no reason.
 */
@Tag("combat")
public class StrengthWatchTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @BeforeEach
    public void resetWatch() {
        StrengthWatch.clearForTests();
    }

    @AfterEach
    public void clearWatch() {
        StrengthWatch.clearForTests();
    }

    @Test
    public void rowsOnlyOnTransitions() {
        world(10, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Zealot, 12, 10)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) StrengthWatch.sample(false);
            if (atlantis.game.A.now() == 2) StrengthWatch.sample(false);
            if (atlantis.game.A.now() == 3) StrengthWatch.sample(true);
            if (atlantis.game.A.now() == 4) StrengthWatch.sample(true);
        });

        String rows = atlantis.util.log.EventLog.toJsonLines();
        assertEquals(2, count(rows),
            "first sample plus the one transition row, steady state is silent: " + rows);
    }

    private int count(String rows) {
        int n = 0;
        int at = -1;
        while ((at = rows.indexOf("\"key\":\"STRENGTH_WATCH\"", at + 1)) >= 0) n++;

        return n;
    }
}
