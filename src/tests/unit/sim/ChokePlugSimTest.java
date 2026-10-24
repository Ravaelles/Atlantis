package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import atlantis.battle.BattleCommander;
import atlantis.battle.advance.focus.AFocusPoint;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Choke plug (owner, PurpleWave SquadDefendBase guard port): against a
 * melee-only enemy the line waits AT the mouth - melee in, dragoons in
 * the row behind - instead of wherever the crescent happens to form.
 * The funnel staging decides it: the army starts a dozen tiles back,
 * the plug marches it to the mouth; the crescent would form up where
 * it stands and let the pack run by the flank.
 */
@Tag("combat")
public class ChokePlugSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    @Test
    public void lineWaitsAtTheMouthMeleeFirst() {
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 5, 20);
        FakeUnit d1 = fake(AUnitType.Protoss_Dragoon, 8, 19);
        FakeUnit d2 = fake(AUnitType.Protoss_Dragoon, 8, 21);
        FakeUnit d3 = fake(AUnitType.Protoss_Dragoon, 9, 20);
        FakeUnit z1 = fake(AUnitType.Protoss_Zealot, 7, 19);
        FakeUnit z2 = fake(AUnitType.Protoss_Zealot, 7, 21);
        FakeUnit z3 = fake(AUnitType.Protoss_Zealot, 8, 20);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(nexus);
        ours.add(d1);
        ours.add(d2);
        ours.add(d3);
        ours.add(z1);
        ours.add(z2);
        ours.add(z3);
        for (FakeUnit d : ours) d.cooldown = 0;

        List<FakeUnit> foes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, 34 + (i % 3), 19 + (i / 3));
            ling.setEnemy();
            foes.add(ling);
        }

        // Vertical wall with a 4-tile gap at y in [18,22): the choke.
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        arena.block(18 * TILE, 0, 2 * TILE, 18 * TILE);
        arena.block(18 * TILE, 22 * TILE, 2 * TILE, 18 * TILE);
        useArenaWalkability(arena);

        FakeChoke choke = new FakeChoke(APosition.create(19, 20), 8);
        AFocusPoint anchor = new AFocusPoint(choke, APosition.create(10, 20), "MainChoke");

        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            try (MockedStatic<atlantis.map.choke.Chokes> chokes =
                Mockito.mockStatic(atlantis.map.choke.Chokes.class)) {
                chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(choke);
                Alpha.gather();

                // Phase 1: staging, no contact yet - the sentry holds still.
                for (int i = 0; i < 200; i++) {
                    final List<FakeUnit> foesNow = foes;
                    final List<FakeUnit> oursNow = ours;
                    world.tick(() -> {
                        for (FakeUnit u : oursNow) {
                            if (u.hp > 0 && u.isCombatUnit()) {
                                BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, d1);
                            }
                        }
                        ZombiePolicies.holdStill(foesNow, oursNow);
                    });
                }

                APosition plug = atlantis.battle.formation.ChokePlug.plugPointFor(choke, nexus);
                assertTrue(plug != null, "the plug names the mouth");
                double plugX = plug.x();

                // Row order, read off the assigned slots (deterministic,
                // march-independent): every zealot slot sits nearer the
                // foe than every dragoon slot.
                double zealotSlotFoe = -1;
                double dragoonSlotFoe = Double.MAX_VALUE;
                for (FakeUnit z : new FakeUnit[] { z1, z2, z3 }) {
                    APosition s = atlantis.battle.formation.ChokePlug.slotFor(
                        z, atlantis.battle.cluster.Alpha.get().selection(), plug, choke.center(),
                        choke.width() / 2.0);
                    assertTrue(s != null, "every zealot gets a plug slot");
                    double sd = s.distTo(choke.center());
                    zealotSlotFoe = Math.max(zealotSlotFoe, sd);
                }
                for (FakeUnit d : new FakeUnit[] { d1, d2, d3 }) {
                    APosition s = atlantis.battle.formation.ChokePlug.slotFor(
                        d, atlantis.battle.cluster.Alpha.get().selection(), plug, choke.center(),
                        choke.width() / 2.0);
                    assertTrue(s != null, "every dragoon gets a plug slot");
                    dragoonSlotFoe = Math.min(dragoonSlotFoe, s.distTo(choke.center()));
                }
                System.out.println("PLUG plug=(" + plug.x() / 32 + "," + plug.y() / 32 + ")"
                    + " zealotSlotFoe=" + zealotSlotFoe
                    + " dragoonSlotFoe=" + dragoonSlotFoe);
                assertTrue(zealotSlotFoe < dragoonSlotFoe,
                    "melee rows sit nearer the foe than the ranged rows");

                // March discipline: the line actually walks to the mouth.
                double dragoonEast = Double.MAX_VALUE;
                double zealotEast = -1;
                for (FakeUnit d : new FakeUnit[] { d1, d2, d3 }) {
                    if (d.hp > 0) dragoonEast = Math.min(dragoonEast, d.x());
                }
                for (FakeUnit z : new FakeUnit[] { z1, z2, z3 }) {
                    if (z.hp > 0) zealotEast = Math.max(zealotEast, z.x());
                }
                System.out.println("PLUG staged zealotEast=" + zealotEast / TILE
                    + " dragoonEast=" + dragoonEast / TILE + " plugX=" + plugX / TILE);

                assertTrue(zealotEast > plugX - 2 * TILE,
                    "melee takes the mouth, not the backyard: zealotEast=" + zealotEast / TILE);
                assertTrue(dragoonEast > plugX - 6 * TILE,
                    "second row stays at shooting distance, not home: " + dragoonEast / TILE);
            }
        }
        finally {
            for (atlantis.units.AUnit u : Alpha.get().selection().list()) Alpha.get().removeUnit(u);
            tests.fakes.FakeMapTiles.installAsSource();
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    private void useArenaWalkability(final SimArena arena) {
        atlantis.map.MapTiles.useSource(new atlantis.map.MapTiles.Source() {
            @Override
            public boolean isWalkable(atlantis.map.position.HasPosition at) {
                APosition p = at.position();

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
            public boolean canBuildHere(atlantis.units.AUnit builder, atlantis.units.AUnitType building, APosition at) {
                return true;
            }
        });
    }
}
