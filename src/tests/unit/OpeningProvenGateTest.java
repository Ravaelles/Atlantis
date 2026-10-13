package tests.unit;

import atlantis.config.env.Env;
import atlantis.production.ForceExitWhenOpeningProven;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The --short gate ends the game on a finished Core AND Dragoons - Dragoons
 * alone once ended the run, which is how a phantom Dragoon count (no gas, no
 * refinery, no Core) proved an opening that never happened. A warping Dragoon
 * is not proof either: the next event could be a cancel.
 */
public class OpeningProvenGateTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void resetGate() {
        Env.setCoreAndDragoonGateForTest(false);
    }

    @Test
    public void corePlusTwoFinishedDragoonsProvesTheOpening() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit core = fake(AUnitType.Protoss_Cybernetics_Core, 12, 10);
        FakeUnit goonA = fake(AUnitType.Protoss_Dragoon, 14, 10);
        FakeUnit goonB = fake(AUnitType.Protoss_Dragoon, 15, 10);

        Env.setCoreAndDragoonGateForTest(true);

        world(31, fakeOurs(nexus, core, goonA, goonB), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 30) return;

            assertTrue(
                new ForceExitWhenOpeningProven().applies(),
                "a finished Core plus two finished Dragoons is the proven opening"
            );
        });
    }

    @Test
    public void dragoonsWithoutACoreProveNothing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit goonA = fake(AUnitType.Protoss_Dragoon, 14, 10);
        FakeUnit goonB = fake(AUnitType.Protoss_Dragoon, 15, 10);

        Env.setCoreAndDragoonGateForTest(true);

        world(31, fakeOurs(nexus, goonA, goonB), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 30) return;

            assertFalse(
                new ForceExitWhenOpeningProven().applies(),
                "Dragoons without a Core must not end the run (the phantom-Dragoon lesson)"
            );
        });
    }

    @Test
    public void warpingDragoonsAreNotProof() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit core = fake(AUnitType.Protoss_Cybernetics_Core, 12, 10);
        FakeUnit goonA = fake(AUnitType.Protoss_Dragoon, 14, 10).setCompleted(false);
        FakeUnit goonB = fake(AUnitType.Protoss_Dragoon, 15, 10).setCompleted(false);

        Env.setCoreAndDragoonGateForTest(true);

        world(31, fakeOurs(nexus, core, goonA, goonB), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 30) return;

            assertFalse(
                new ForceExitWhenOpeningProven().applies(),
                "Dragoons still warping in are not proof - the next event could be a cancel"
            );
        });
    }
}
