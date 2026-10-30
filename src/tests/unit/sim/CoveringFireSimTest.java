package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Covering fire (ladder rule 6, NEXT #184): a healthy Dragoon covers the
 * ling chewing a withdrawing ally instead of its nearest foe. Removal was
 * attempted and WITHDRAWN: without the rule the gun takes its nearest
 * (L2) while L1 chews the withdrawer 5x longer (l1DiedAt 9 vs 46) and W
 * leaves with 45 hp instead of 55. The rule is load-bearing for
 * withdrawer protection. This scenario stays as its pin: H must open on
 * the chewer and W must keep most of its hp.
 */
@Tag("combat")
public class CoveringFireSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    @Test
    public void healthyGunCoversTheChewer() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit w = fake(AUnitType.Protoss_Dragoon, 36, 20);
        w.hp = 60;
        w.shields = 0;
        w.cooldown = 0;
        FakeUnit h = fake(AUnitType.Protoss_Dragoon, 33, 20);
        h.hp = 160;
        h.shields = 80;
        h.cooldown = 0;
        FakeUnit t = fake(AUnitType.Protoss_Dragoon, 31, 21);
        t.hp = 160;
        t.shields = 80;
        t.cooldown = 0;
        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 36.5, 20);
        l1.setEnemy();
        l1.hp = 35;
        FakeUnit l2 = fake(AUnitType.Zerg_Zergling, 34, 22);
        l2.setEnemy();
        l2.hp = 35;
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(w);
        ours.add(h);
        ours.add(t);
        List<FakeUnit> foes = new ArrayList<>();
        foes.add(l1);
        foes.add(l2);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[]{w, h, t, nexus}, foes.toArray(new FakeUnit[0]), arena);
        atlantis.map.MapTiles.useSource(new atlantis.map.MapTiles.Source() {
            @Override
            public boolean isWalkable(atlantis.map.position.HasPosition at) {
                atlantis.map.position.APosition p = at.position();
                return arena.isWalkablePx(p.x(), p.y());
            }

            @Override
            public boolean isExplored(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean isVisible(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean isBuildable(atlantis.map.position.HasPosition at, boolean alsoCheckBuildings) {
                return true;
            }

            @Override
            public boolean hasPathBetween(atlantis.map.position.HasPosition from, atlantis.map.position.HasPosition to) {
                return true;
            }

            @Override
            public boolean canBuildHere(atlantis.units.AUnit builder, atlantis.units.AUnitType building,
                                        atlantis.map.position.APosition at) {
                return true;
            }
        });
        APosition anchor = APosition.create(60, 20);

        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            Alpha.gather();

            StringBuilder firstShots = new StringBuilder();
            int l1DiedAt = -1;
            int clearAt = -1;
            for (int i = 0; i < 1200; i++) {
                world.tick(() -> {
                    if (l1.hp > 0 && w.hp > 0) l1.attackUnit(w);
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, h);
                    }
                });
                if (h.target != null && firstShots.length() == 0) {
                    firstShots.append("h->").append(h.target.id() == l1.id() ? "L1" : "L2")
                        .append('@').append(i);
                }
                if (l1.hp <= 0 && l1DiedAt < 0) l1DiedAt = i;
                if (l1.hp <= 0 && l2.hp <= 0) {
                    clearAt = i;
                    break;
                }
            }

            assertTrue(firstShots.toString().startsWith("h->L1"),
                "the healthy gun opens on the chewer, not its nearest foe: " + firstShots);
            assertTrue(l1DiedAt > 0 && l1DiedAt < 60,
                "covered chewer dies fast: " + l1DiedAt);
            assertTrue(w.hp >= 50,
                "covering protects the withdrawer: " + w.hp);
            assertTrue(clearAt > 0 && clearAt < 400,
                "the pair clears: " + clearAt);
            assertTrue(h.hp > 0 && w.hp > 0,
                "both Dragoons survive the meeting engagement");
        } finally {
            world.close();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            tests.fakes.FakeMapTiles.installAsSource();
        }
    }
}
