package tests.unit;

import atlantis.production.GoalBoardFeeder;
import atlantis.production.dynamic.protoss.tech.ResearchProtossGroundArmor;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import bwapi.UpgradeType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeResearch;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;
import static tests.fakes.FakeUnitHelper.merge;

/**
 * B-42: UPGRADE_COMPLETED never fired live because the observation sat in
 * handle(), behind the decision gate - a completed upgrade makes the gate
 * refuse ("researched"), so the completion was never seen. The
 * observation now lives in the base applies(), which runs every visited
 * frame: even a refusing gate still witnesses the level increase.
 *
 * <p>Shape pinned here, armor edition (no static side effects, unlike the
 * Singularity range flip): weapons L1 done opens the armor gate, armor
 * 0 is recorded, armor 1 refuses as researched - and the twin fires
 * exactly once. The two reads sit 35 frames apart to outlast the ATech
 * 31-frame cache, the way the live 6:06-to-7:53 window does.</p>
 */
@Tag("production")
public class UpgradeCompletedBehindGateTest extends AbstractTestWithWorld {

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

    @BeforeEach
    public void hermetic() {
        GoalBoardFeeder.clearForTests();
        EventLog.clearForTests();
    }

    @AfterEach
    public void clean() {
        EventLog.clearForTests();
        GoalBoardFeeder.clearForTests();
    }

    @Test
    public void refusingGateStillWitnessesCompletion() {
        currentMinerals = 800;
        currentGas = 400;
        currentSupplyUsed = 40;
        currentSupplyTotal = 60;

        FakeUnit[] zealots = new FakeUnit[12];
        for (int i = 0; i < 12; i++) zealots[i] = fake(AUnitType.Protoss_Zealot, 30 + i, 20);
        FakeUnit[] ours = merge(
            fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Forge, 24, 20)
            ),
            zealots
        );

        final boolean[] refusedAtMax = new boolean[]{false};
        final int[] rowsAtBaseline = new int[]{-1};

        world(50, ours, fakeEnemies(fake(AUnitType.Zerg_Zergling, 60, 20)), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) EventLog.clearForTests();
            if (now == 5) {
                FakeResearch.withUpgradeLevel(UpgradeType.Protoss_Ground_Weapons, 1);
                FakeResearch.withUpgradeLevel(UpgradeType.Protoss_Ground_Armor, 0);
                new ResearchProtossGroundArmor().applies();
                rowsAtBaseline[0] = countUpgradeCompletedRows();
            }
            if (now == 40) {
                // Fresh source: the ATech 31-frame cache would otherwise
                // serve the level-0 reading from frame 5. Reinstalling is
                // what the next game does naturally; the stub must see the
                // rise the way the live 6:06-to-7:53 window did.
                FakeResearch.installAsSource();
                FakeResearch.withUpgradeLevel(UpgradeType.Protoss_Ground_Weapons, 1);
                FakeResearch.withUpgradeLevel(UpgradeType.Protoss_Ground_Armor, 3);
                refusedAtMax[0] = !new ResearchProtossGroundArmor().applies();
            }
        });

        assertEquals(0, rowsAtBaseline[0], "first sighting records the baseline, never a row");
        assertTrue(refusedAtMax[0], "armor maxed: the gate refuses as researched");
        assertEquals(1, countUpgradeCompletedRows(),
            "the refusing gate still witnessed the 0-to-3 rise exactly once: "
                + EventLog.toJsonLines());
    }

    private static int countUpgradeCompletedRows() {
        String log = EventLog.toJsonLines();
        int count = 0;
        int from = 0;
        while (true) {
            int key = log.indexOf("\"key\":\"UPGRADE_COMPLETED\"", from);
            if (key < 0) return count;

            int end = log.indexOf('\n', key);
            if (end < 0) end = log.length();
            String row = log.substring(key, end);
            if (row.contains("\"upgrade\":\"Protoss_Ground_Armor\"")
                && row.contains("\"level\":3")) {
                count++;
            }
            from = end;
        }
    }
}
