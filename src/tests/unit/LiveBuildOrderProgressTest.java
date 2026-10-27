package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.BuildOrderGoals;
import atlantis.production.v2.goals.BuildOrderRow;
import atlantis.production.v2.engine.LiveBuildOrderProgress;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A placed-but-not-yet-warping construction counts as produced (live
 * duplicate shape, 20261027_121013: the second Pylon dispatched at f3602,
 * 83 frames after the first, because the arrived order sat IN_PROGRESS
 * with no unit and produced() went blind for the arrival-to-warp window).
 *
 * <p>NOT_STARTED and STARTING already count ({@code countsAsDemand});
 * IN_PROGRESS with a linked unit counts as an unfinished unit. Only the
 * unitless IN_PROGRESS was invisible - and that is exactly the window a
 * walking builder's arrival opens.
 */
@Tag("production")
public class LiveBuildOrderProgressTest extends AbstractTestWithWorld {

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

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void placedButNotYetWarpingConstructionCountsAsProduced() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 22, 22)), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            currentSupplyUsed = 17;

            // One standing Pylon plus one arrived order: placed, builder
            // committed, warp not started yet, no unit anywhere.
            Construction arrived = new Construction(AUnitType.Protoss_Pylon);
            arrived.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            ConstructionRequests.constructions.add(arrived);

            List<BuildOrderRow> rows = Arrays.asList(
                BuildOrderRow.of(UnitProducible.of(AUnitType.Protoss_Pylon), 8),
                BuildOrderRow.of(UnitProducible.of(AUnitType.Protoss_Pylon), 15));

            assertTrue(
                BuildOrderGoals.from(rows, new LiveBuildOrderProgress()).isEmpty(),
                "standing plus arrived is two Pylons: no row may re-fire while the warp starts");
        });
    }
}
