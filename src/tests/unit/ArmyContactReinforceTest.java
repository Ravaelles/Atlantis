package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.ArmyContact;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Split-cluster reinforcement (NEXT #181, owner 2026-10-26): one part of
 * the army is being shredded while the other half holds formation slots
 * with no target. The idle part marches at the freshest fight instead
 * of waiting; a stale contact marches nowhere.
 */
@Tag("combat")
public class ArmyContactReinforceTest extends WorldStubForTests {

    @Test
    public void idleHalfMarchesToTheFreshFight() {
        aGame.when(atlantis.game.AGame::isUms).thenReturn(true);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 40, 20);
        FakeUnit enemy = fake(AUnitType.Protoss_Zealot, 22, 20);
        enemy.setEnemy();

        world(10, fakeOurs(a, b), fakeEnemies(enemy), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                BattleCommander.act(a, FightOrFlight.Tactic.FIGHT, null, a);

                return;
            }
            if (now != 2) return;

            // No target in reach for B, no ghost for B, formation needs
            // 3+ units: the only path marching exactly at the fight
            // point is the reinforce. Gather would aim at the leader,
            // formation at an offset slot - neither lands here.
            BattleCommander.act(b, FightOrFlight.Tactic.FIGHT, null, b);
            assertNotNull(b.targetPosition, "B marches instead of holding");
            assertTrue(b.targetPosition.distTo(enemy.position()) < 1.0,
                "at the fresh fight, not at a slot or the leader: " + b.targetPosition);
        });
    }

    @Test
    public void staleContactMarchesNowhere() {
        aGame.when(atlantis.game.AGame::isUms).thenReturn(true);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 40, 20);
        FakeUnit enemy = fake(AUnitType.Protoss_Zealot, 22, 20);
        enemy.setEnemy();

        world(300, fakeOurs(a, b), fakeEnemies(enemy), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                BattleCommander.act(a, FightOrFlight.Tactic.FIGHT, null, a);

                return;
            }
            if (now != 290) return;

            BattleCommander.act(b, FightOrFlight.Tactic.FIGHT, null, b);
            assertTrue(b.tooltip() == null || !b.tooltip().contains("ClusterReinforce"),
                "a 289-frame-old contact is history, not a destination: " + b.tooltip());
        });
    }

    @Test
    public void meleeKeepsItsDiscipline() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 40, 20);
        FakeUnit enemy = fake(AUnitType.Protoss_Zealot, 22, 20);
        enemy.setEnemy();

        world(10, fakeOurs(a, b), fakeEnemies(enemy), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                BattleCommander.act(a, FightOrFlight.Tactic.FIGHT, null, a);

                return;
            }
            if (now != 2) return;

            // No UMS stub: melee keeps anchor/formation discipline, a
            // scouting skirmish never drags the army across the map.
            BattleCommander.act(b, FightOrFlight.Tactic.FIGHT, null, b);
            assertTrue(b.targetPosition == null
                || b.targetPosition.distTo(enemy.position()) >= 1.0,
                "melee does not march at fresh contacts: " + b.targetPosition);
        });
    }

    @Test
    public void arrivedUnitIsNotRetasked() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 22, 20);
        FakeUnit enemy = fake(AUnitType.Protoss_Zealot, 23, 20);
        enemy.setEnemy();

        world(10, fakeOurs(a, b), fakeEnemies(enemy), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                BattleCommander.act(a, FightOrFlight.Tactic.FIGHT, null, a);

                return;
            }
            if (now != 2) return;

            assertNull(ArmyContact.reinforcePointFor(b),
                "already at the fight: nothing to join");
        });
    }

    @Test
    public void noContactMeansNoReinforce() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 40, 20);

        world(3, fakeOurs(a, b), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertNull(ArmyContact.reinforcePointFor(b),
                "nobody fights: the formation fallback owns the unit as before");
        });
    }

    @Test
    public void ongoingFightIsThreeSeconds() {
        aGame.when(atlantis.game.AGame::isUms).thenReturn(true);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit enemy = fake(AUnitType.Protoss_Zealot, 22, 20);
        enemy.setEnemy();

        world(150, fakeOurs(a), fakeEnemies(enemy), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                BattleCommander.act(a, FightOrFlight.Tactic.FIGHT, null, a);
            }
            if (now == 2) {
                assertTrue(ArmyContact.isOngoingFight(),
                    "one frame after the report the fight is ongoing");
            }
            if (now == 100) {
                assertFalse(ArmyContact.isOngoingFight(),
                    "99 frames of silence: old news, not an ongoing fight");
            }
        });
    }

    @Test
    public void ongoingFightBeatsFormationSlots() {
        // Owner order: nobody waits in formation while the cluster fights.
        // Three ours, so slots exist and the old order would take one -
        // with a one-frame-old fight the reinforce marches at the fight.
        aGame.when(atlantis.game.AGame::isUms).thenReturn(true);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 40, 20);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 42, 22);
        FakeUnit enemy = fake(AUnitType.Protoss_Zealot, 22, 20);
        enemy.setEnemy();

        world(10, fakeOurs(a, b, c), fakeEnemies(enemy), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                BattleCommander.act(a, FightOrFlight.Tactic.FIGHT, null, a);

                return;
            }
            if (now != 2) return;

            BattleCommander.act(b, FightOrFlight.Tactic.FIGHT, null, b);
            assertTrue(b.targetPosition != null
                && b.targetPosition.distTo(enemy.position()) < 5.0,
                "B joins the ongoing fight instead of a slot: " + b.targetPosition);
            assertTrue(b.commandHistory().toString().contains("ClusterReinforce"),
                "the march is tagged reinforce, not a slot: " + b.commandHistory());
        });
    }

    @Test
    public void idleArmyKeepsItsSlots() {
        // Same board, no fight anywhere: the slot fallback still owns
        // the unit - the reorder only fires on an ongoing fight.
        aGame.when(atlantis.game.AGame::isUms).thenReturn(true);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 40, 20);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 42, 22);
        FakeUnit enemy = fake(AUnitType.Protoss_Zealot, 60, 20);
        enemy.setEnemy();

        world(10, fakeOurs(a, b, c), fakeEnemies(enemy), () -> {
            if (atlantis.game.A.now() != 2) return;

            BattleCommander.act(b, FightOrFlight.Tactic.FIGHT, null, b);
            assertTrue(!b.commandHistory().toString().contains("ClusterReinforce"),
                "no fight, no reinforce - slots/ghost/gather as before: " + b.commandHistory());
        });
    }
}
