package tests.unit;

import atlantis.battle.twins.HitTwin;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Every hit a fighting unit takes names itself (lost HP+shields,
 * threat distance, cooldown, moving) so a late-game bleed is
 * attributable instead of guesswork. Sampling every frame, rows
 * only when health went missing.
 */
public class HitTwinTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void cleanLog() {
        EventLog.clearForTests();
        HitTwin.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        HitTwin.clearForTests();
    }

    @Test
    public void hitNamesLostDistCooldownAndMoving() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            HitTwin.sample(dragoon, zealot);
            dragoon.hp -= 8;
            HitTwin.sample(dragoon, zealot);

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"HIT\""), "got: " + rows);
            assertTrue(rows.contains("\"lost\":8"), "got: " + rows);
            assertTrue(rows.contains("\"cooldown\":15"), "got: " + rows);
            assertTrue(rows.contains("\"moving\":"), "got: " + rows);
            assertTrue(rows.contains("\"tx\":"), "threat position rides along: " + rows);
        });
    }

    @Test
    public void noRowWithoutDamage() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            HitTwin.sample(dragoon, zealot);
            HitTwin.sample(dragoon, zealot);

            assertFalse(EventLog.toJsonLines().contains("\"key\":\"HIT\""));
        });
    }

    @Test
    public void shieldDamageCounts() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            HitTwin.sample(dragoon, zealot);
            dragoon.shields -= 16;
            HitTwin.sample(dragoon, zealot);

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"HIT\""), "got: " + rows);
            assertTrue(rows.contains("\"lost\":16"), "got: " + rows);
        });
    }
}
