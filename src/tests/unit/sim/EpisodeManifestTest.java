package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * The episode manifest pins here: header shape, one row per tick with
 * the acting unit's features, and dead-unit rows that stay parseable.
 * The learner reads this text, never the sim internals.
 */
@Tag("combat")
public class EpisodeManifestTest extends tests.acceptance.WorldStubForTests {

    @Test
    public void manifestHasHeaderPlusOneRowPerTick() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 120;
        d.shields = 80;
        d.cooldown = 0;
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 33, 20);
        ling.setEnemy();

        SimArena arena = new SimArena(70 * 32, 40 * 32);
        SimWorld world = new SimWorld(
            new FakeUnit[] { nexus, d }, new FakeUnit[] { ling }, arena);
        try {
            BattleCommander.clearForTests();
            Alpha.gather();

            List<String> rows = new ArrayList<>();
            for (int f = 0; f < 10; f++) {
                final int frame = f;
                world.tick(() -> {
                    if (d.hp > 0) BattleCommander.act(d, FightOrFlight.Tactic.FIGHT, null, d);
                    ZombiePolicies.chaseNearest(
                        new ArrayList<>(java.util.Arrays.asList(ling)),
                        new ArrayList<>(java.util.Arrays.asList(d)));
                    rows.add(EpisodeManifest.render(frame, d));
                });
            }

            String manifest = EpisodeManifest.renderAll(rows);
            String[] lines = manifest.split("\n");
            assertEquals(11, lines.length, "header + 10 ticks");
            assertTrue(lines[0].startsWith("frame,unit,hpFrac"), "header names every column");
            for (int i = 1; i < lines.length; i++) {
                assertEquals(9, lines[i].split(",").length,
                    "every row has 9 fields: " + lines[i]);
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
        }
    }

    @Test
    public void deadUnitsStayParseable() {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 0;

        String row = EpisodeManifest.render(7, d);
        assertEquals(9, row.split(",").length, "dead rows keep the shape: " + row);
        assertTrue(row.startsWith("7,"), "frame first: " + row);
    }
}
