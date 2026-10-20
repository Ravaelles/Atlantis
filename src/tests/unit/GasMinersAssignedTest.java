package tests.unit;

import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.fakes.FakeUnitHelper.merge;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Game 162906: Assimilator stood all game with gas=0 in all 177 bank
 * samples, yet exactly one Dragoon trained (gas flowed transiently). The
 * assignment formula must demand miners with 22 workers and empty bank -
 * anything else is the suspect, not the formula.
 *
 * <p>Full miner-to-refinery flow is not pinnable in stub (fake units carry
 * no engine resource fields); game-side telemetry (assignment lines) names
 * the rest.</p>
 */
@Tag("production")
public class GasMinersAssignedTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void twentyTwoWorkersDemandThreeGasMiners() {
        FakeUnit[] crew = new FakeUnit[22];
        for (int i = 0; i < 22; i++) crew[i] = fake(AUnitType.Protoss_Probe, 21, 21);

        world(5, merge(
            fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Assimilator, 24, 20)
            ),
            crew
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertEquals(
                3, atlantis.units.buildings.GasWorkersPerBuilding.define(),
                "22 workers on empty bank must want 3 gas miners (162906 mined 0)");
        });
    }
}
