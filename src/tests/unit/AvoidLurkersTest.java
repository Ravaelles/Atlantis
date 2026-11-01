package tests.unit;

import atlantis.battle.avoid.special.AvoidLurkers;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner UMS Lurkers2: a Lurker that STARTED burrowing is already
 * unhittable without a detector over it, and Zealots standing next
 * to it die during the transition - the burrowed-only filter never
 * saw it coming. A burrowing Lurker nearby means Zealots leave,
 * unconditionally (no hp/army bravery vetoes); detector cover at the
 * unit is the only exception.
 */
@Tag("combat")
public class AvoidLurkersTest extends WorldStubForTests {

    private FakeUnit[] tenFriends() {
        FakeUnit[] friends = new FakeUnit[10];
        for (int i = 0; i < 10; i++) {
            friends[i] = fake(AUnitType.Protoss_Zealot, 31 + i, 20);
        }
        return friends;
    }

    @Test
    public void zealotFleesBurrowingLurkerDespiteFullHpAndBigArmy() {
        // The unconditional half: full-hp Zealot in an 11-strong group
        // next to a single Lurker - every bravery veto says stand, but
        // the burrowing transition already made it unhittable.
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 35, 20);
        lurker.setEnemy();
        lurker.setBurrowing(true);

        FakeUnit[] friends = tenFriends();
        FakeUnit[] ours = new FakeUnit[1 + friends.length];
        ours[0] = zealot;
        System.arraycopy(friends, 0, ours, 1, friends.length);

        world(70, fakeOurs(ours), fakeEnemies(lurker), () -> {
            assertTrue(new AvoidLurkers(zealot).applies(),
                "burrowing started 5 tiles out: Zealots leave, no vetoes apply");
        });
    }

    @Test
    public void detectorCoverIsTheOnlyException() {
        // Same board plus an Observer standing at the unit: the Lurker
        // stays hittable (effVisible), the normal rules own the call -
        // and they say stand (full hp, big army, single Lurker).
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit observer = fake(AUnitType.Protoss_Observer, 30, 20);
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 35, 20);
        lurker.setEnemy();
        lurker.setBurrowing(true);

        FakeUnit[] friends = tenFriends();
        FakeUnit[] ours = new FakeUnit[2 + friends.length];
        ours[0] = zealot;
        ours[1] = observer;
        System.arraycopy(friends, 0, ours, 2, friends.length);

        world(70, fakeOurs(ours), fakeEnemies(lurker), () -> {
            assertFalse(new AvoidLurkers(zealot).applies(),
                "detector at the unit keeps the Lurker hittable - stand and kill it");
        });
    }

    @Test
    public void visibleUnburrowedLurkerTriggersNothingUnconditional() {
        // No transition, no trigger: a merely visible Lurker near a
        // wounded Zealot answers through the normal filters only (here:
        // burrowed-only, so nothing - the Lurker stands unburrowed).
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        zealot.setHp(50);
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 35, 20);
        lurker.setEnemy();

        world(70, fakeOurs(zealot), fakeEnemies(lurker), () -> {
            assertFalse(new AvoidLurkers(zealot).applies(),
                "no burrowing started: the unconditional branch stays silent");
        });
    }

    @Test
    public void burrowingBeyondReachTriggersNothing() {
        // The leash: a burrowing Lurker 14 tiles out is outside the
        // avoid radius - no panic across the map.
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 60, 20);
        lurker.setEnemy();
        lurker.setBurrowing(true);

        world(70, fakeOurs(zealot), fakeEnemies(lurker), () -> {
            assertFalse(new AvoidLurkers(zealot).applies(),
                "burrowing outside the avoid radius is not our problem");
        });
    }

    @Test
    public void armyZealotBreaksOffThroughBattlePipeline() {
        // The wiring pin (owner UMS Lurkers2): the avoid manager ran
        // only for workers and scouts, so the army never evaluated it.
        // Through BattleCommander the burrowing Lurker owns the Zealot's
        // frame with an avoid move instead of a ghost creep or a stand.
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 36, 20);
        lurker.setEnemy();
        lurker.setBurrowing(true);

        world(70, fakeOurs(nexus, zealot), fakeEnemies(lurker), () -> {
            int now = atlantis.game.A.now();
            if (now < 2) return;

            atlantis.combat.BattleCommander.act(
                zealot, atlantis.combat.cluster.FightOrFlight.Tactic.FIGHT, null, zealot);

            if (now != 29) return;

            assertTrue(zealot.commandHistory().toString().contains("RUN_ENEMY"),
                "the army Zealot breaks off with an avoid run, never a creep: "
                    + zealot.commandHistory());
            assertFalse(zealot.commandHistory().toString().contains("ClusterGhost"),
                "no ghost march at the unhittable transition: " + zealot.commandHistory());
        });
    }

    @Test
    public void avoidMoveDoorIssuesThroughTheHook() {
        // Diagnostic companion 2: does the move itself work on the stub,
        // or does runFrom die inside the fake world (map finder, notify)?
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 36, 20);
        lurker.setEnemy();
        lurker.setBurrowing(true);

        world(70, fakeOurs(nexus, zealot), fakeEnemies(lurker), () -> {
            int now = atlantis.game.A.now();
            if (now != 29) return;

            AvoidLurkers avoid = new AvoidLurkers(zealot);
            assertTrue(avoid.applies(), "gate must hold here");
            assertNotNull(avoid.forceHandle(), "the move door must issue");
        });
    }
}
