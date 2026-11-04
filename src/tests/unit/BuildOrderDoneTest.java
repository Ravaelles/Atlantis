package tests.unit;

import atlantis.production.demand.BuildOrderGoals;
import atlantis.production.demand.BuildOrderProgress;
import atlantis.production.demand.BuildOrderRow;
import atlantis.production.producibles.Producible;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * "Build order done" reads the same rows with the same progress the
 * scheduler's own BO goals read (owner order 2026-11-04): a parallel
 * structure (like the legacy queue) can silently disagree with the
 * door it gates. Pure decision table, no game.
 */
@Tag("production")
public class BuildOrderDoneTest {

    private static BuildOrderRow row(AUnitType type, int minSupply) {
        return new BuildOrderRow(UnitProducible.of(type), minSupply, 1, null);
    }

    private static BuildOrderProgress progress(final int producedGateways, final int producedPylons) {
        return new BuildOrderProgress() {
            @Override
            public int supplyUsed() {
                return 20;
            }

            @Override
            public int supplyFree() {
                return 5;
            }

            @Override
            public int produced(Producible item) {
                if (item.equals(UnitProducible.of(AUnitType.Protoss_Gateway))) return producedGateways;
                if (item.equals(UnitProducible.of(AUnitType.Protoss_Pylon))) return producedPylons;

                return 99;
            }
        };
    }

    @Test
    public void unfinishedRowMeansNotDone() {
        List<BuildOrderRow> rows = new ArrayList<>(Arrays.asList(
            row(AUnitType.Protoss_Pylon, 8),
            row(AUnitType.Protoss_Gateway, 10),
            row(AUnitType.Protoss_Gateway, 12)));

        assertFalse(BuildOrderGoals.isFinished(rows, progress(1, 1)),
            "one Gateway built of two demanded keeps the opening open");
    }

    @Test
    public void allRowsProducedMeansDone() {
        List<BuildOrderRow> rows = new ArrayList<>(Arrays.asList(
            row(AUnitType.Protoss_Pylon, 8),
            row(AUnitType.Protoss_Gateway, 10),
            row(AUnitType.Protoss_Gateway, 12)));

        assertTrue(BuildOrderGoals.isFinished(rows, progress(2, 1)),
            "every row produced means the BO is done");
    }

    @Test
    public void emptyOrNullReadsAsDoneNeverBlocked() {
        assertTrue(BuildOrderGoals.isFinished(new ArrayList<BuildOrderRow>(), progress(0, 0)),
            "nothing demanded is done");
        assertTrue(BuildOrderGoals.isFinished(null, progress(0, 0)),
            "no rows reads as done, never as blocked");
    }
}
