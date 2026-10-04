package tests.acceptance;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicProductionCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import atlantis.util.Counter;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitData;
import tests.unit.DynamicMockOurUnits;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B-22: does the Protoss dynamic unit production keep working?
 *
 * <p>Reported from a real game: "we build a zealot and a dragoon and then nothing,
 * even with resources". The path that decides this is
 * {@code ProtossDynamicUnitProductionCommander.handle()}, and until now **no test
 * could reach it**: it starts with {@code if (!AGame.everyNthGameFrame(7)) return
 * false}, and {@code AGame} is statically mocked in every test, so an unstubbed
 * static method answered false and the whole commander stood still. 77 call sites
 * in 65 files share that throttle; {@code useFakeTime} now answers it from the same
 * frame number as everything else.</p>
 *
 * <p>The world is the reported one: 800 minerals, 300 gas, supply 40 of 60, one
 * zealot and one dragoon on the map, a gateway with a cybernetics core behind it,
 * two Marines and an SCV for company. What is asserted is the property the report
 * is about - production <b>keeps</b> going, not that one particular unit type wins:
 * with this much resource and a free gateway, at least two more combat units get
 * ordered inside the run.</p>
 *
 * <p>Measured, and the answer is not the one the report expected: 300 frames, one
 * order every 7 (the commander's own throttle), **42 Dragoons and no zealots at
 * all**. Zealot production is last in the chain and Dragoon answers first at this
 * economy, so "a zealot and a dragoon and then nothing" is not what this code does
 * when it is asked - which moves the report to a question about whether the
 * commander is asked at all in a real game, not about these gates.</p>
 *
 * <p>The harness has to finish the training for the commander to be asked again: a
 * stub gateway stays busy forever, because nothing in the stub world turns "ordered"
 * into "finished". The test materialises each ordered unit the way the engine would
 * after 25 seconds, which is what {@code TerranDynamicUnitsCommanderTest} does too.</p>
 */
public class ProtossDynamicCombatProductionTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private final ArrayList<FakeUnit> spawned = new ArrayList<>();
    private int trainedCombatUnits = 0;

    @Test
    public void richProtossKeepsOrderingCombatUnits() {
        currentMinerals = 800;
        currentGas = 300;

        FakeUnit[] ours = fakeOurs(
            fake(Protoss_Nexus, 10),
            fake(Protoss_Pylon, 11),
            fake(Protoss_Gateway, 12),
            fake(Protoss_Cybernetics_Core, 13),
            fake(Protoss_Assimilator, 14),
            fake(Protoss_Photon_Cannon, 15),
            fake(Protoss_Probe, 16),
            fake(Protoss_Probe, 17),
            fake(Protoss_Zealot, 18),
            fake(Protoss_Dragoon, 19)
        );
        FakeUnit[] enemies = fakeEnemies(
            fakeEnemy(Terran_Marine, 40),
            fakeEnemy(Terran_Marine, 41),
            fakeEnemy(Terran_SCV, 42)
        );

        world(300, ours, enemies, () -> {
            if (A.now() == 1) {
                initSupply(40, 60);
                assertEquals(40, A.supplyUsed(), "the supply the report describes");
                assertEquals(800, A.minerals(), "and the resources it describes");
                assertEquals(300, A.gas(), "including gas, which zealots do not want");
            }
            else {
                (new DynamicProductionCommander()).forceHandle();
                finishWhatWasJustOrdered();
            }
        });

        // Zealot or Dragoon: the report is that the bot stopped producing, not that it
        // chose the other unit. Anything else it ordered (a building, a tech) does not
        // answer the question.
        assertTrue(trainedCombatUnits >= 2,
            "800 minerals, 300 gas and a free gateway should keep producing combat "
                + "units, ordered: " + orderedSoFar());
    }

    // =========================================================

    /**
     * The engine finishes a training after ~25 seconds; the stub world has to be told,
     * or the gateway is busy forever and the commander is never asked again. Only
     * units recorded by the order sink are materialised - that list is exactly "what
     * the bot ordered".
     */
    private void finishWhatWasJustOrdered() {
        if (FakeUnitData.TRAIN.size() == spawned.size()) {
            return;
        }

        for (int i = spawned.size(); i < FakeUnitData.TRAIN.size(); i++) {
            AUnitType type = FakeUnitData.TRAIN.get(i);
            spawned.add(fake(type, 25 + i * 0.5));

            if (type == Protoss_Zealot || type == Protoss_Dragoon) {
                trainedCombatUnits++;
            }
        }

        ArrayList<FakeUnit> ourUnits = new ArrayList<>();
        ourUnits.add(fake(Protoss_Nexus, 10));
        ourUnits.add(fake(Protoss_Pylon, 11));
        ourUnits.add(fake(Protoss_Gateway, 12));
        ourUnits.add(fake(Protoss_Cybernetics_Core, 13));
        ourUnits.add(fake(Protoss_Assimilator, 14));
        ourUnits.add(fake(Protoss_Photon_Cannon, 15));
        ourUnits.add(fake(Protoss_Probe, 16));
        ourUnits.add(fake(Protoss_Probe, 17));
        ourUnits.add(fake(Protoss_Zealot, 18));
        ourUnits.add(fake(Protoss_Dragoon, 19));
        ourUnits.addAll(spawned);

        DynamicMockOurUnits.mockOur(ourUnits);
        Queue.get().refresh();
    }

    private String orderedSoFar() {
        Counter<AUnitType> counter = new Counter<>();
        for (AUnitType type : FakeUnitData.TRAIN) {
            counter.incrementValueFor(type);
        }

        StringBuilder result = new StringBuilder();
        for (AUnitType type : counter.keys()) {
            result.append(type).append("=").append(counter.getValueFor(type)).append(" ");
        }
        return result.toString();
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}