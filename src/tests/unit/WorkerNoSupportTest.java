package tests.unit;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.defence.fight.WorkerDefenceFightCombatUnits;
import atlantis.units.workers.defence.fight.WorkerDefenceFightWorkers;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-26 (live 7th_rav game): a worker that is both losing badly
 * (eval at or below 0.4) AND alone (no combat-unit support nearby) must not
 * fight - and a worker at 16 hp or less fights nothing. The swarm is exempt
 * on purpose: few raiders with enough workers around is the won fight the
 * swarm exists to take (WorkerSwarmTest pins a 3-probe attack at eval
 * 0.145), so the ban must not fire on the swarm shape.
 */
@Tag("combat")
public class WorkerNoSupportTest extends WorldStubForTests {

    private static final String FIGHT_SOURCE =
        "src/atlantis/units/workers/defence/fight/WorkerDefenceFightCombatUnits.java";
    private static final String WORKERS_SOURCE =
        "src/atlantis/units/workers/defence/fight/WorkerDefenceFightWorkers.java";

    private static String read(String path) throws Exception {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)),
            java.nio.charset.StandardCharsets.UTF_8);
    }

    @Test
    public void loneProbeLosingBadlyAndAloneDoesNotFight() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 20);
        probe.hp = 35;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 21.5, 20);
        zealot.setEnemy();

        world(20, fakeOurs(nexus, probe), fakeEnemies(zealot), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertTrue(worker.hasEnemyForEval(), "a zealot next door is an evaluable fight");

            double eval = worker.eval();
            assertTrue(eval <= 0.4,
                "this is the bad-and-alone shape the rule is about (eval=" + eval + ")");

            WorkerDefenceFightCombatUnits fight = new WorkerDefenceFightCombatUnits(worker);
            assertTrue(fight.applies(), "the setup must reach the shared gate, not die in applies()");
            assertFalse(fight.forceHandled(), "a lone losing probe must not attack with no support (eval=" + eval + ")");
        });
    }

    @Test
    public void woundedWorkerDoesNotFightEnemyWorkers() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 20);
        probe.hp = 15;
        FakeUnit e1 = fake(AUnitType.Protoss_Probe, 21.5, 20);
        e1.setEnemy();
        FakeUnit e2 = fake(AUnitType.Protoss_Probe, 22, 20);
        e2.setEnemy();

        world(20, fakeOurs(nexus, probe), fakeEnemies(e1, e2), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertFalse(new WorkerDefenceFightWorkers(worker).applies(), "a worker at 15 hp fights nothing, even enemy workers");

            probe.hp = 20;
            assertTrue(new WorkerDefenceFightWorkers(worker).applies(),
                "a healthy worker still answers enemy workers");
        });
    }

    @Test
    public void theGateAndItsExemptionAreWired() throws Exception {
        String fight = read(FIGHT_SOURCE);

        assertTrue(fight.contains("EVAL_DO_NOT_FIGHT_BADLY = 0.4"),
            "the bad-and-alone floor must be the owner's 0.4");
        assertTrue(fight.contains("unit.eval() <= EVAL_DO_NOT_FIGHT_BADLY"),
            "the low eval must actually be compared before attacking");
        assertTrue(fight.contains("!hasCombatSupportNear(unit)"),
            "the ban needs the missing support, or lone probes could never swarm");
        assertTrue(fight.contains("!isSwarmShapedFight(unit)"),
            "the swarm shape must exempt the won fight the swarm exists to take");
        assertTrue(fight.contains("BadEvalNoSupport"),
            "the refusal must carry its own tooltip");

        String workers = read(WORKERS_SOURCE);
        assertTrue(workers.contains("hp() > 16"),
            "the 16-hp floor must hold even against enemy workers");
    }
}
