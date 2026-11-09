package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterAnchor;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.focus.MiddleFocusChoke;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Rally reads the task (owner 2026-11-09, MDC enforcement): a line that
 * holds the natural choke and loses the fight re-forms ON the natural
 * line instead of running home past it. Failing shape first: today the
 * retreat home is the main area and the floor only stops it at the
 * main-choke line, abandoning the natural the mission owns.
 */
@Tag("combat")
public class RetreatToNaturalChokeTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ArmyMission.clearForTests();
        atlantis.combat.focus.ProtossDefendFocusPoint.clearForTests();
        EventLog.clearForTests();
        BattleCommander.clearForTests();
    }

    @Test
    public void losingAtNaturalReformsOnTheNaturalLine() {
        enemyRaceInWorld = Race.Zerg;

        AChoke mainChoke = Mockito.mock(AChoke.class);
        Mockito.when(mainChoke.center()).thenReturn(atlantis.map.position.APosition.create(60, 20));
        FakeChoke naturalChoke = new FakeChoke(atlantis.map.position.APosition.create(44, 40), 3);

        FakeUnit mainNexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 40, 44);
        FakeUnit[] army = new FakeUnit[]{
            fake(AUnitType.Protoss_Zealot, 43, 39),
            fake(AUnitType.Protoss_Zealot, 44, 40),
            fake(AUnitType.Protoss_Zealot, 45, 41),
            fake(AUnitType.Protoss_Zealot, 43, 41)};
        FakeUnit[] foes = new FakeUnit[]{
            fake(AUnitType.Zerg_Zergling, 49, 37),
            fake(AUnitType.Zerg_Zergling, 50, 38),
            fake(AUnitType.Zerg_Zergling, 48, 36),
            fake(AUnitType.Zerg_Zergling, 51, 39),
            fake(AUnitType.Zerg_Zergling, 49, 35),
            fake(AUnitType.Zerg_Zergling, 50, 36)};
        for (FakeUnit foe : foes) foe.setEnemy();

        try (MockedStatic<atlantis.map.choke.Chokes> chokes =
                 Mockito.mockStatic(atlantis.map.choke.Chokes.class);
             MockedStatic<atlantis.map.base.Bases> bases =
                 Mockito.mockStatic(atlantis.map.base.Bases.class);
             MockedStatic<MiddleFocusChoke> middle =
                 Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(mainChoke);
            chokes.when(atlantis.map.choke.Chokes::natural).thenReturn(naturalChoke);
            bases.when(atlantis.map.base.Bases::natural).thenReturn(natural);
            middle.when(MiddleFocusChoke::get).thenReturn(null);

            FakeUnit[] ours = new FakeUnit[]{mainNexus, natural,
                army[0], army[1], army[2], army[3]};
            world(5, fakeOurs(ours), fakeEnemies(foes), () -> {
                if (atlantis.game.A.now() != 2) return;

                for (FakeUnit u : army) Alpha.get().addUnit(u);
                try {
                    ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);
                    assertEquals(ArmyMission.HOLD_NATURAL_CHOKE, ArmyMission.current(),
                        "dressed at the stub natural choke, the mission forbids leaving");

                    AUnit leader = Alpha.get().leader();
                    assertNotNull(leader, "the cluster has a leader");
                    HasPosition anchor = ClusterAnchor.defendAnchor();
                    RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
                        army[0], FightOrFlight.Tactic.FLEE, anchor, leader);

                    assertNotNull(verdict.dest, "a losing line gets a rally, not a stand");
                    assertTrue(
                        verdict.dest.distTo(naturalChoke.center())
                            < verdict.dest.distTo(mainNexus.position()),
                        "the rally holds the natural line instead of running home: "
                            + verdict.dest);
                    assertTrue(
                        verdict.dest.distTo(naturalChoke.center())
                            < verdict.dest.distTo(mainChoke.center()),
                        "the rally holds the natural line instead of running home past it: "
                            + verdict.dest);
                } finally {
                    for (FakeUnit u : army) Alpha.get().removeUnit(u);
                    ArmyMission.clearForTests();
                }
            });
        }
    }
}
