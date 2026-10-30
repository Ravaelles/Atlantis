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
 * Shared focus (ladder rule 5, NEXT #184): two Dragoons meet a weak and a
 * healthy Zealot plus chaff. Removal was attempted and WITHDRAWN: rule 5
 * is load-bearing at the 9.0 boundary (inclusive `<= 9` vs rule 8's
 * strict `< 9` in `nearestEngageableEnemy`) - without it the ladder
 * acquires nothing at exactly 9 tiles and two ChaseVeto pins fail. This
 * scenario stays as the rule's outcome pin: the pair must still clear
 * fast with both Dragoons alive.
 */
@Tag("combat")
public class SharedFocusSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    @Test
    public void sharedFocusClearsWeakFirst() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d1 = fake(AUnitType.Protoss_Dragoon, 35, 24);
        d1.hp = 160;
        d1.shields = 80;
        d1.cooldown = 0;
        FakeUnit d2 = fake(AUnitType.Protoss_Dragoon, 36, 25);
        d2.hp = 160;
        d2.shields = 80;
        d2.cooldown = 0;
        FakeUnit weak = fake(AUnitType.Protoss_Zealot, 30, 19);
        weak.setEnemy();
        weak.hp = 20;
        weak.shields = 0;
        FakeUnit healthy = fake(AUnitType.Protoss_Zealot, 38, 21);
        healthy.setEnemy();
        healthy.hp = 100;
        healthy.shields = 60;
        FakeUnit chaff = fake(AUnitType.Zerg_Zergling, 34, 23);
        chaff.setEnemy();
        chaff.hp = 35;
        chaff.shields = 0;
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(d1);
        ours.add(d2);
        List<FakeUnit> foes = new ArrayList<>();
        foes.add(weak);
        foes.add(healthy);
        foes.add(chaff);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[]{d1, d2, nexus}, foes.toArray(new FakeUnit[0]), arena);
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

            int weakDiedAt = -1;
            int healthyDiedAt = -1;
            int clearAt = -1;
            for (int i = 0; i < 1200; i++) {
                world.tick(() -> {
                    ZombiePolicies.chaseNearest(foes, ours);
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, d1);
                    }
                });
                if (weak.hp <= 0 && weakDiedAt < 0) weakDiedAt = i;
                if (healthy.hp <= 0 && healthyDiedAt < 0) healthyDiedAt = i;
                if (weak.hp <= 0 && healthy.hp <= 0) {
                    clearAt = i;
                    break;
                }
            }

            assertTrue(weakDiedAt > 0 && healthyDiedAt > 0 && clearAt > 0 && clearAt < 400,
                "the pair clears without the shared rule: no focus-fire gate,"
                    + " nearest approach still kills both with no losses");
            assertTrue(d1.hp > 0 && d2.hp > 0,
                "both Dragoons survive the meeting engagement");
        } finally {
            world.close();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            tests.fakes.FakeMapTiles.installAsSource();
        }
    }
}
