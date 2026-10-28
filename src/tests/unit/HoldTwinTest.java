package tests.unit;

import atlantis.combat.FormationHold;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.twins.HoldTwin;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HOLD twin (NEXT #182): every hold decision names its verdict - hold,
 * or which gate broke it (veto/contact/damage/kite/unstuck/no-slot/
 * move-failed) - with the nearest enemy distance beside it, so the
 * next "stood into chews" report is decidable from logs instead of
 * forensics. Throttled: verdict changes always row; a steady verdict
 * only heartbeats.
 */
@Tag("combat")
public class HoldTwinTest extends WorldStubForTests {

    @BeforeEach
    public void cleanLogBefore() {
        EventLog.clearForTests();
        HoldTwin.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        HoldTwin.clearForTests();
    }

    @Test
    public void holdVerdictRowsNameTheGate() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 32.5, 20);
        foe.setEnemy();

        world(3, fakeOurs(dragoon), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            HoldTwin.note(dragoon, foe, FightOrFlight.Tactic.FIGHT,
                FormationHold.HoldVerdict.VETO);
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"HOLD\""), "got: " + rows);
            assertTrue(rows.contains("\"verdict\":\"VETO\""), "got: " + rows);
        });
    }

    @Test
    public void steadyVerdictDoesNotFlood() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 32.5, 20);
        foe.setEnemy();

        // 200 frames of the same verdict: only the opening row plus
        // heartbeats may land, never one row per frame.
        world(200, fakeOurs(dragoon), fakeEnemies(foe), () -> {
            HoldTwin.note(dragoon, foe, FightOrFlight.Tactic.FIGHT,
                FormationHold.HoldVerdict.HOLD);
        });

        String rows = EventLog.toJsonLines();
        int count = 0;
        int from = 0;
        String needle = "\"key\":\"HOLD\"";
        while (true) {
            int at = rows.indexOf(needle, from);
            if (at < 0) break;
            count++;
            from = at + needle.length();
        }
        assertTrue(count >= 1 && count <= 3, "throttled, not flooding: " + count);
    }

    /**
     * NEXT #188 MOVE_FAILED follow-up: the 373x quiet refusals are
     * BWAPI-level {@code u().move()} false, so the twin names the
     * engine state at refuse time (canCommand/canMove) instead of
     * another stack trace. Uniform across verdicts: the holders are
     * the control group the coming unstick decision compares against.
     */
    @Test
    public void moveFailedRowsNameBwapiRefusalState() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 32.5, 20);
        foe.setEnemy();
        dragoon.canCommand = false;
        dragoon.canMove = false;

        world(3, fakeOurs(dragoon), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            HoldTwin.note(dragoon, foe, FightOrFlight.Tactic.FIGHT,
                FormationHold.HoldVerdict.MOVE_FAILED);
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"verdict\":\"MOVE_FAILED\""), "got: " + rows);
            assertTrue(rows.contains("\"canCmd\":false") && rows.contains("\"canMove\":false"),
                "the refusal names the engine state that refused it: " + rows);
        });
    }

    @Test
    public void holdersRowTheCommandableControlGroup() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 32.5, 20);
        foe.setEnemy();

        world(3, fakeOurs(dragoon), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            HoldTwin.note(dragoon, foe, FightOrFlight.Tactic.FIGHT,
                FormationHold.HoldVerdict.HOLD);
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"canCmd\":true") && rows.contains("\"canMove\":true"),
                "commandable holders row true - the control group: " + rows);
        });
    }

    @Test
    public void verdictFlipAlwaysRows() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 32.5, 20);
        foe.setEnemy();

        world(3, fakeOurs(dragoon), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            HoldTwin.note(dragoon, foe, FightOrFlight.Tactic.FIGHT,
                FormationHold.HoldVerdict.HOLD);
            HoldTwin.note(dragoon, foe, FightOrFlight.Tactic.FIGHT,
                FormationHold.HoldVerdict.DAMAGE);
            HoldTwin.note(dragoon, foe, FightOrFlight.Tactic.FIGHT,
                FormationHold.HoldVerdict.HOLD);
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"verdict\":\"HOLD\""), "got: " + rows);
            assertTrue(rows.contains("\"verdict\":\"DAMAGE\""), "got: " + rows);
        });
    }

    @Test
    public void absorbedReissueRowsLatchHeld() {
        // NEXT #210: the latch absorbs identical formation re-issues, and
        // the calm used to be invisible (no twin row on the deny path).
        // Same geometry as the latch's own deny pin: first call issues,
        // second is absorbed - and the absorbed one rows LATCH_HELD.
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 22, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 60, 20);
        foe.setEnemy();
        foe.hp = 100;
        FakeUnit[] pack = new FakeUnit[]{
            fake(AUnitType.Protoss_Zealot, 61, 20),
            fake(AUnitType.Protoss_Zealot, 62, 20),
            fake(AUnitType.Protoss_Zealot, 60, 21),
            fake(AUnitType.Protoss_Zealot, 61, 21),
            fake(AUnitType.Protoss_Zealot, 62, 21),
        };
        for (FakeUnit enemy : pack) enemy.setEnemy();

        world(5, fakeOurs(nexus, a, b, c),
            fakeEnemies(foe, pack[0], pack[1], pack[2], pack[3], pack[4]), () -> {
            if (atlantis.game.A.now() != 2) return;

            for (FakeUnit u : new FakeUnit[]{a, b, c}) atlantis.battle.cluster.Alpha.get().addUnit(u);
            try {
                atlantis.map.position.APosition anchor =
                    atlantis.map.position.APosition.create(30, 20);
                assertTrue(FormationHold.holdIfCalm(
                    a, foe, FightOrFlight.Tactic.FIGHT, a, anchor),
                    "first deployment goes out");
                assertTrue(FormationHold.holdIfCalm(
                    a, foe, FightOrFlight.Tactic.FIGHT, a, anchor),
                    "the denied re-issue still holds");
                String rows = EventLog.toJsonLines();
                assertTrue(rows.contains("\"verdict\":\"LATCH_HELD\""),
                    "the absorbed re-issue names the calm: " + rows);
            } finally {
                for (FakeUnit u : new FakeUnit[]{a, b, c}) atlantis.battle.cluster.Alpha.get().removeUnit(u);
            }
        });
    }
}
