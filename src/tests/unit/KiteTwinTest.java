package tests.unit;

import atlantis.combat.kite.KiteTwin;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every kiting step names itself (dist x10, cooldown, result) so a kite
 * that stops is attributable instead of guesswork. Steps throttle to
 * one row per unit per 30 frames; stuck steps always land.
 */
@Tag("combat")
public class KiteTwinTest extends WorldStubForTests {

    private static int countRows(String text, String needle) {
        int count = 0;
        int from = 0;
        while (true) {
            int at = text.indexOf(needle, from);
            if (at < 0) return count;

            count++;
            from = at + needle.length();
        }
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void cleanLog() {
        EventLog.clearForTests();
        KiteTwin.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        KiteTwin.clearForTests();
    }

    @Test
    public void stepNamesDistCooldownAndResult() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            KiteTwin.steppedAside(dragoon, zealot);
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"KITE\""), "got: " + rows);
            assertTrue(rows.contains("\"result\":\"stepped-aside\""), "got: " + rows);
            assertTrue(rows.contains("\"cooldown\":15"), "got: " + rows);
            assertTrue(rows.contains("\"ux\":"), "positions ride along: " + rows);
        });
    }

    @Test
    public void everyStepRowsSoHitsAttributeExactly() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        world(400, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            int now = atlantis.game.A.now();
            if (now != 3 && now != 10 && now != 320) return;

            KiteTwin.steppedAside(dragoon, zealot);
        });

        int steps = countRows(EventLog.toJsonLines(), "\"result\":\"stepped-aside\"");
        assertEquals(3, steps, "no throttle - every step must row for hit timing: " + steps);
    }

    @Test
    public void stuckStepAlwaysLands() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 25;

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            KiteTwin.steppedAside(dragoon, zealot);
            KiteTwin.nowhereToStep(dragoon, zealot);
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"result\":\"stuck-no-step\""), "got: " + rows);
        });
    }

    @Test
    public void noTwinWithoutKite() {
        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20)), fakeEnemies(), () -> {
            assertFalse(EventLog.toJsonLines().contains("\"key\":\"KITE\""));
        });
    }
}
