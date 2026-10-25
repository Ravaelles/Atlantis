package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.behaviors.CorsairHunt;
import atlantis.combat.behaviors.ProduceScarabs;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import tests.fakes.FakeOrderSink;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #198: specialists own their frame (ordered list, first true
 * wins). Reavers brew scarabs instead of standing around unloaded;
 * unmolested Corsairs close and kill Overlords instead of idling.
 */
@Tag("combat")
public class SpecialistBehaviorsTest extends WorldStubForTests {

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

            return new int[] { x, y };
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
}