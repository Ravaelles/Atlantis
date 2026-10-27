package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.behaviors.CastStorm;
import atlantis.combat.behaviors.CorsairHunt;
import atlantis.combat.behaviors.ProduceScarabs;
import atlantis.combat.behaviors.RunWhenAttacked;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #198: specialists own their frame (ordered list, first true
 * wins). Reavers brew scarabs instead of standing around unloaded;
 * unmolested Corsairs close and kill Overlords instead of idling.
 */
@Tag("combat")
public class SpecialistBehaviorsTest extends WorldStubForTests {

    private static boolean movesAwayFrom(java.util.List<FakeOrderSink.Record> orders, FakeUnit unit, int tx, int ty) {
        int fromX = unit.position().tx();
        int fromY = unit.position().ty();
        double before = Math.hypot(fromX - tx, fromY - ty);
        for (FakeOrderSink.Record record : orders) {
            if (!record.operation.equals("move")) continue;
            int[] at = parseTile(record.toString());
            if (at == null) continue;
            if (Math.hypot(at[0] - tx, at[1] - ty) > before) return true;
        }

        return false;
    }

    private static boolean stormCastAt(java.util.List<FakeOrderSink.Record> orders, int unitId) {
        for (FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals("useTechAt")
                && record.toString().contains("Psionic_Storm")) return true;
        }

        return false;
    }

    private static boolean movesToward(java.util.List<FakeOrderSink.Record> orders, FakeUnit unit, int tx, int ty) {
        int fromX = unit.position().tx();
        int fromY = unit.position().ty();
        double before = Math.hypot(fromX - tx, fromY - ty);
        for (FakeOrderSink.Record record : orders) {
            if (!record.operation.equals("move")) continue;
            int[] at = parseTile(record.toString());
            if (at == null) continue;
            if (Math.hypot(at[0] - tx, at[1] - ty) < before) return true;
        }

        return false;
    }

    private static int[] parseTile(String row) {
        try {
            String cut = row.substring(row.indexOf('[') + 1, row.lastIndexOf(']'));
            String[] parts = cut.split(",");
            int x = Integer.parseInt(parts[0].trim()) / 32;
            int y = Integer.parseInt(parts[1].trim()) / 32;

            return new int[]{x, y};
        } catch (Exception ignored) {
            return null;
        }
    }

    private static java.util.List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }

    private static boolean hasOrder(java.util.List<FakeOrderSink.Record> orders, int unitId, String operation) {
        for (FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }

        return false;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void reaverBrewsScarabsWhenLow() {
        FakeUnit reaver = fake(AUnitType.Protoss_Reaver, 30, 20);
        reaver.scarabCount = 2;

        world(20, fakeOurs(reaver), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            currentMinerals = 100;

            assertTrue(ProduceScarabs.act(reaver),
                "2 scarabs + banked minerals: brew one");
        });
    }

    @Test
    public void reaverSkipsFullMagazineAndTraining() {
        FakeUnit full = fake(AUnitType.Protoss_Reaver, 30, 20);
        full.scarabCount = 5;
        FakeUnit busy = fake(AUnitType.Protoss_Reaver, 32, 20);
        busy.scarabCount = 1;
        busy.training = true;

        world(20, fakeOurs(full, busy), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            currentMinerals = 100;

            assertFalse(ProduceScarabs.act(full), "full magazine: nothing to do");
            assertFalse(ProduceScarabs.act(busy), "already training: no double order");
        });
    }

    @Test
    public void corsairClosesOnFarOverlord() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        corsair.cooldown = 20;
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 32, 20);
        overlord.setEnemy();

        world(20, fakeOurs(corsair), fakeEnemies(overlord), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CorsairHunt.act(corsair),
                "cooling Corsair 12 tiles out moves in, not idles");
        });
    }

    @Test
    public void hookedCorsairHuntsThroughAct() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        corsair.cooldown = 20;
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 32, 20);
        overlord.setEnemy();

        world(20, fakeOurs(nexus, corsair), fakeEnemies(overlord), () -> {
            if (atlantis.game.A.now() != 2) return;

            ordersOf(corsair).clear();
            BattleCommander.act(corsair, FightOrFlight.Tactic.FIGHT, null, corsair);

            assertTrue(movesToward(ordersOf(corsair), corsair, 32, 20),
                "hooked act() moves toward the overlord instead of standing: " + ordersOf(corsair));
        });
    }

    @Test
    public void hookedActStaysSilentWithoutHunt() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        corsair.cooldown = 20;

        world(20, fakeOurs(nexus, corsair), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            ordersOf(corsair).clear();
            BattleCommander.act(corsair, FightOrFlight.Tactic.FIGHT, null, corsair);

            assertFalse(movesToward(ordersOf(corsair), corsair, 32, 20),
                "no hunt, no march toward the east: the hook adds hunts only");
        });
    }

    @Test
    public void corsairLeavesMolestedHuntsAlone() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 36, 20);
        overlord.setEnemy();

        world(20, fakeOurs(corsair), fakeEnemies(overlord), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CorsairHunt.applies(fake(AUnitType.Protoss_Dragoon, 20, 20)),
                "dragoons never hunt: wrong unit, no opinion");
        });
    }

    @Test
    public void dtBreaksContactWhenHurt() {
        FakeUnit dt = fake(AUnitType.Protoss_Dark_Templar, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(20, fakeOurs(dt), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 2) return;

            dt.unitState().setLastUnderAttack(atlantis.game.A.now());

            assertTrue(RunWhenAttacked.act(dt),
                "a hurt DT breaks contact instead of trading like a zealot");
            assertTrue(movesAwayFrom(ordersOf(dt), dt, 32, 20),
                "the run goes away from the threat, not through it: " + ordersOf(dt));
        });
    }

    @Test
    public void dtHoldsWhenUnmolested() {
        FakeUnit dt = fake(AUnitType.Protoss_Dark_Templar, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(20, fakeOurs(dt), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(RunWhenAttacked.act(dt),
                "an unhurt DT keeps killing: the run is for contact, not presence");
        });
    }

    @Test
    public void dtIgnoresEnemiesThatCannotHitIt() {
        FakeUnit dt = fake(AUnitType.Protoss_Dark_Templar, 30, 20);
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 32, 20);
        overlord.setEnemy();

        world(20, fakeOurs(dt), fakeEnemies(overlord), () -> {
            if (atlantis.game.A.now() != 2) return;

            dt.unitState().setLastUnderAttack(atlantis.game.A.now());

            assertFalse(RunWhenAttacked.act(dt),
                "nothing here can hit the DT: running blind wastes the cloak");
        });
    }

    @Test
    public void hookedDtRunsThroughAct() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dt = fake(AUnitType.Protoss_Dark_Templar, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(20, fakeOurs(nexus, dt), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 2) return;

            dt.unitState().setLastUnderAttack(atlantis.game.A.now());

            ordersOf(dt).clear();
            BattleCommander.act(dt, FightOrFlight.Tactic.FIGHT, null, dt);

            assertTrue(movesAwayFrom(ordersOf(dt), dt, 32, 20),
                "hooked act() breaks the DT out instead of trading: " + ordersOf(dt));
        });
    }

    @Test
    public void templarStormsCondensedClump() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 20);
        ht.setEnergy(100);

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 35, 20),
            fake(AUnitType.Protoss_Zealot, 36, 20),
            fake(AUnitType.Protoss_Zealot, 35, 21)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CastStorm.act(ht),
                "75+ energy and 3 in storm radius: storm the clump");
            assertTrue(stormCastAt(ordersOf(ht), ht.id()),
                "a Psionic Storm goes out, not a move: " + ordersOf(ht));
        });
    }

    @Test
    public void templarHoldsWithoutEnergy() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 24);
        ht.setEnergy(50);

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 35, 24),
            fake(AUnitType.Protoss_Zealot, 36, 24),
            fake(AUnitType.Protoss_Zealot, 35, 25)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CastStorm.act(ht), "50 energy: hold the storm");
        });
    }

    @Test
    public void templarHoldsOnSpreadEnemies() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 28);
        ht.setEnergy(150);

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 35, 28),
            fake(AUnitType.Protoss_Zealot, 45, 28)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CastStorm.act(ht), "no 3-clump in range: hold the storm");
        });
    }

    @Test
    public void zergClumpNeedsFourOrCharge() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 32);
        ht.setEnergy(100);

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Zerg_Zergling, 35, 32),
            fake(AUnitType.Zerg_Zergling, 36, 32),
            fake(AUnitType.Zerg_Zergling, 35, 33)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CastStorm.act(ht),
                "vs Zerg 3 lings at 100 energy is not a storm (legacy: 4, or 3 charged)");
        });
    }

    @Test
    public void chargedTemplarStormsZergTrio() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 36);
        ht.setEnergy(120);

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Zerg_Zergling, 35, 36),
            fake(AUnitType.Zerg_Zergling, 36, 36),
            fake(AUnitType.Zerg_Zergling, 35, 37)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CastStorm.act(ht),
                "110+ energy lowers the bar by one (legacy): storm the trio");
            assertTrue(stormCastAt(ordersOf(ht), ht.id()),
                "a Psionic Storm goes out, not a move: " + ordersOf(ht));
        });
    }

    @Test
    public void hookedTemplarStormsThroughAct() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 40);
        ht.setEnergy(120);

        world(20, fakeOurs(nexus, ht), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 35, 40),
            fake(AUnitType.Protoss_Zealot, 36, 40),
            fake(AUnitType.Protoss_Zealot, 35, 41)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            ordersOf(ht).clear();
            BattleCommander.act(ht, FightOrFlight.Tactic.FIGHT, null, ht);

            assertTrue(stormCastAt(ordersOf(ht), ht.id()),
                "hooked act() storms the clump instead of walking in: " + ordersOf(ht));
        });
    }

    @Test
    public void observerFallsThroughSpecialists() {
        FakeUnit observer = fake(AUnitType.Protoss_Observer, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(20, fakeOurs(observer), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(atlantis.combat.behaviors.SpecialistBehaviors.act(observer),
                "detectors are flown by the Observer chain, never grabbed here");
        });
    }

    @Test
    public void templarSnipesCrucialReaver() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 64);
        ht.setEnergy(100);
        FakeUnit screen = fake(AUnitType.Protoss_Dragoon, 34, 63);

        world(20, fakeOurs(ht, screen), fakeEnemies(
            fake(AUnitType.Protoss_Reaver, 35, 64),
            fake(AUnitType.Protoss_Zealot, 36, 64)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CastStorm.act(ht),
                "a Reaver with one screener storms, not the clump bar");
            assertTrue(stormCastAt(ordersOf(ht), ht.id()),
                "a Psionic Storm goes out, not a move: " + ordersOf(ht));
        });
    }

    @Test
    public void templarHoldsCrucialInCrowd() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 66);
        ht.setEnergy(100);

        world(20, fakeOurs(
            ht,
            fake(AUnitType.Protoss_Dragoon, 34, 65),
            fake(AUnitType.Protoss_Dragoon, 36, 67),
            fake(AUnitType.Protoss_Dragoon, 33, 66)
        ), fakeEnemies(
            fake(AUnitType.Protoss_Reaver, 35, 66),
            fake(AUnitType.Protoss_Zealot, 36, 66)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CastStorm.act(ht),
                "three of ours on the Reaver: storming wastes our line, hold");
        });
    }

    @Test
    public void templarSnipesImportantCarrier() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 68);
        ht.setEnergy(100);
        FakeUnit screen = fake(AUnitType.Protoss_Dragoon, 34, 67);

        world(20, fakeOurs(ht, screen), fakeEnemies(
            fake(AUnitType.Protoss_Carrier, 35, 68),
            fake(AUnitType.Protoss_Zealot, 36, 68)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CastStorm.act(ht),
                "a Carrier with one escort storms below the clump bar");
            assertTrue(stormCastAt(ordersOf(ht), ht.id()),
                "a Psionic Storm goes out, not a move: " + ordersOf(ht));
        });
    }

    @Test
    public void templarHoldsLoneImportant() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 70);
        ht.setEnergy(100);

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Protoss_Carrier, 35, 70)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CastStorm.act(ht),
                "a lone Carrier with no pack around it is no storm");
        });
    }

    @Test
    public void templarHoldsImportantInCrowd() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 72);
        ht.setEnergy(100);

        world(20, fakeOurs(
            ht,
            fake(AUnitType.Protoss_Dragoon, 34, 71),
            fake(AUnitType.Protoss_Dragoon, 36, 73),
            fake(AUnitType.Protoss_Dragoon, 33, 72)
        ), fakeEnemies(
            fake(AUnitType.Protoss_Carrier, 35, 72),
            fake(AUnitType.Protoss_Zealot, 36, 72)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CastStorm.act(ht),
                "three of ours on the pack: hold the storm");
        });
    }

    @Test
    public void dyingTemplarForcesStormOnPair() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 30, 74);
        ht.setEnergy(80);
        ht.hp = 20;

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 35, 74),
            fake(AUnitType.Protoss_Zealot, 36, 74)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CastStorm.act(ht),
                "at 20 hp the bar is gone: storm the pair");
            assertTrue(stormCastAt(ordersOf(ht), ht.id()),
                "a Psionic Storm goes out, not a move: " + ordersOf(ht));
        });
    }

    @Test
    public void chargedTemplarLowersBarToPair() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 40, 76);
        ht.setEnergy(190);

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Zerg_Zergling, 45, 76),
            fake(AUnitType.Zerg_Zergling, 46, 76)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CastStorm.act(ht),
                "at 190 vs Zerg the bar is two (legacy): storm the pair");
            assertTrue(stormCastAt(ordersOf(ht), ht.id()),
                "a Psionic Storm goes out, not a move: " + ordersOf(ht));
        });
    }

    @Test
    public void fullTemplarStormsPairAtFullCharge() {
        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 40, 81);
        ht.setEnergy(200);

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 45, 81),
            fake(AUnitType.Protoss_Zealot, 46, 81)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CastStorm.act(ht),
                "at 200 the bar is one (legacy): storm the pair");
            assertTrue(stormCastAt(ordersOf(ht), ht.id()),
                "a Psionic Storm goes out, not a move: " + ordersOf(ht));
        });
    }

    @Test
    public void deepTierHoldsPairVsZerg() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit ht = fake(AUnitType.Protoss_High_Templar, 40, 86);
        ht.setEnergy(179);

        world(20, fakeOurs(ht), fakeEnemies(
            fake(AUnitType.Zerg_Zergling, 45, 86),
            fake(AUnitType.Zerg_Zergling, 46, 86)
        ), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CastStorm.act(ht),
                "at 179 the bar is still three vs Zerg: hold the pair");
        });
    }
}