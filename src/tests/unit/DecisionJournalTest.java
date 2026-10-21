package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Order journal (owner: army corpses died with an empty history - the static
 * battle functions never called usedManager, so managerLogs stayed blank).
 *
 * <p>Every issued move, attack and stop lands in the unit's last-10 journal
 * with its frame, whoever drove the unit. The death log prints it next to
 * the manager list: this is the "which orders killed it" list.</p>
 */
@Tag("unit")
public class DecisionJournalTest extends WorldStubForTests {

    @Test
    public void issuedMovesLandInTheJournalInOrder() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(), () -> {
            dragoon.move(APosition.create(32, 20), Actions.MOVE_ENGAGE, "ClusterReengage", true);
            dragoon.move(APosition.create(31, 21), Actions.MOVE_AVOID, "ClusterKite", true);

            String journal = dragoon.commandHistory().toString();
            assertTrue(journal.contains("ClusterReengage"), "first order kept: " + journal);
            assertTrue(journal.contains("ClusterKite"), "second order kept: " + journal);
            assertTrue(journal.indexOf("ClusterReengage") < journal.indexOf("ClusterKite"),
                "journal order matches issue order: " + journal);
        });
    }

    @Test
    public void journalKeepsOnlyTheLastTen() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(), () -> {
            for (int i = 0; i < 13; i++) {
                dragoon.move(
                    APosition.create(30 + i, 20), Actions.MOVE_ENGAGE, "Tag" + i, true);
            }

            String journal = dragoon.commandHistory().toString();
            assertTrue(journal.contains("Tag12"), "newest kept: " + journal);
            assertTrue(journal.contains("Tag3"), "10th kept: " + journal);
            assertFalse(journal.contains("Tag2"), "11th dropped, ring of 10: " + journal);
        });
    }

    @Test
    public void consecutiveDuplicateRefreshesInsteadOfAppending() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(2, fakeOurs(dragoon), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            dragoon.move(APosition.create(32, 20), Actions.MOVE_ENGAGE, "ClusterReengage", true);
            dragoon.move(APosition.create(33, 20), Actions.MOVE_ENGAGE, "ClusterReengage", true);

            assertEquals(1, dragoon.commandHistory().messages(
                atlantis.game.A.now(), System.currentTimeMillis() / 1000).size(),
                "same tag twice rides one row: " + dragoon.commandHistory());
        });
    }

    @Test
    public void attacksLandInTheJournal() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            dragoon.attackUnit(zealot);

            assertTrue(dragoon.commandHistory().toString().contains("Attack:"),
                "attack recorded with target: " + dragoon.commandHistory());
        });
    }

    @Test
    public void armyActWritesDecisionsWithoutManagers() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 24);

        world(2, fakeOurs(nexus, dragoon), fakeEnemies(zealot), () -> {
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertFalse(dragoon.commandHistory().isEmpty(),
                "army path journals without any Manager chain");
            assertTrue(dragoon.commandHistory().toString().contains("Cluster"),
                "army branch tags name the decision: " + dragoon.commandHistory());
        });
    }
}
