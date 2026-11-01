package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-11-01: no formation against buildings-only. Slots exist to
 * approach defended positions; against an undefended building the line
 * walks in and kills instead of holding standoff forever (measured:
 * 12 Dragoons held 600 ticks vs a lone Nexus, HOLD x40, zero damage).
 * Red-first: the line held and the Nexus stood untouched.
 */
@Tag("combat")
@Tag("full-tier")
public class NoFormationVsBuildingsOnlyTest extends AbstractTestWithUnits {

    private static FakeUnit dragoon(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Dragoon, tx, ty);
        z.hp = 180;
        z.shields = 20;
        z.cooldown = 0;
        return z;
    }

    @Test
    public void dragoonsWalkIntoAnUndefendedNexusInsteadOfHolding() {
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, 110, 110);
        foeNexus.setEnemy();
        foeNexus.hp = 1500;

        List<FakeUnit> ours = new ArrayList<>();
        ours.add(dragoon(88, 88));
        ours.add(dragoon(89, 88));
        ours.add(dragoon(90, 88));
        List<FakeUnit> foes = new ArrayList<>(Arrays.asList(foeNexus));

        SimArena arena = new SimArena(130 * 32, 130 * 32);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 1500; i++) {
                world.tick(() -> {
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, foeNexus.position(), u);
                    }
                });
                if (foeNexus.hp <= 0) break;
            }

            int holds = 0;
            for (String row : atlantis.util.log.EventLog.toJsonLines().split("\n")) {
                if (row.contains("\"key\":\"HOLD\"") && row.contains("\"verdict\":\"HOLD\"")) holds++;
            }
            assertTrue(holds == 0,
                "no formation against an undefended building (HOLD verdicts: " + holds + ")");
            assertTrue(foeNexus.hp < 1500,
                "the line walks in and kills instead of holding standoff (nexus hp: "
                    + foeNexus.hp + ")");
        } finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }
}
