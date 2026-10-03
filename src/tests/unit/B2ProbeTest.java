package tests.unit;

import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

/**
 * SCRATCH - mechanism probe for _AI/BUGS.md B-2. Delete after measuring.
 */
public class B2ProbeTest extends WorldStubForTests {

    @Test
    public void measureDoomedUnitEvalAsTerran() {
        FakeUnit wraith = fake(AUnitType.Terran_Wraith, 90);
        FakeUnit cannon1 = fakeEnemy(AUnitType.Protoss_Photon_Cannon, 92);
        FakeUnit cannon2 = fakeEnemy(AUnitType.Protoss_Photon_Cannon, 93);

        world(1, fakeOurs(wraith), fakeEnemies(cannon1, cannon2), () -> {
            System.err.println("B2PROBE wraith.eval=" + wraith.eval()
                + " absolute=" + wraith.combatEvalAbsolute()
                + " cannon1.eval=" + cannon1.eval());
        });
    }
}
