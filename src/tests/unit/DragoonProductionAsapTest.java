package tests.unit;

import atlantis.production.dynamic.protoss.units.ProduceDragoon;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;
import tests.fakes.FakeUnitData;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Dragoons must be ordered the moment they are affordable - Core, a free
 * Gateway and exactly the 125/50 price on the first call, not frames later.
 *
 * <p>
 * Measured need (owner's Wine game): Dragoons appeared only once gas passed
 * 60+. The log autopsy showed gas never even reached 50 in that game
 * (Assimilator online ~3:58, ~32 gas mined by the 4:57 end) - so no gate was
 * ever slow, the price gate simply never opened. This test pins the other
 * half: the instant the price IS there, the order goes out on the first call.
 * </p>
 */
@Tag("production")
public class DragoonProductionAsapTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void dragoonOrderedAtExactlyThePrice() {
        currentMinerals = 125;
        currentGas = 50;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 24, 24),
            fake(AUnitType.Protoss_Cybernetics_Core, 26, 26),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            int before = dragoonsInTrainLog();
            ProduceDragoon.dragoon();

            assertTrue(
                dragoonsInTrainLog() > before,
                "Core + free Gateway + exactly 125/50: the first Dragoon must"
                    + " be ordered on the first call, not frames later - reason: "
                    + ProduceDragoon.reason);
        });
    }

    private static int dragoonsInTrainLog() {
        int dragoons = 0;
        for (AUnitType type : FakeUnitData.TRAIN) {
            if (type == AUnitType.Protoss_Dragoon) dragoons++;
        }
        return dragoons;
    }
}
