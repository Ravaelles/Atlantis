package tests.unit;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A waiting PREPARING site next to a finished sibling building is owed
 * demand, not a finish.
 *
 * <p><b>Measured (Wine log):</b> {@code PREPARING Gateway at [96,111] for
 * supply 12} printed 6 times in 8 game-seconds - same tile, same supply.
 * A re-print means re-creation means the link was dropped between passes:
 * the proximity finish saw the finished first Gateway within 2.5 tiles,
 * FINISHED the not-yet-producible second row's site, the list lost it, the
 * orphan pass unlinked the live order, and the preparer re-picked the same
 * (free) tile. The #57 exemption covered only adopted NOT_STARTED rows; a
 * builderless PREPARING site holds no worker (nothing to free) and its
 * order is not producible yet, so finishing it only spins.</p>
 *
 * <p>Kept deliberately: builderless NOT_STARTED rows still finish (old
 * path), and a PREPARING site with a builder held on it stays visible to
 * the old path too. An actually occupied site fails loudly at
 * adoption/issue time instead of spinning here.</p>
 */
@Tag("placement")
public class PreparingSiteSurvivesSiblingTest extends AbstractTestWithWorld {

    private boolean fired = false;

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        fired = false;
    }

    @Test
    public void preparingNextToFinishedSiblingIsNotFinished() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 90, 110);

        world(5, units(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Gateway, 96, 111),
            probe
        ), new FakeUnit[0], () -> {
            if (A.now() != 3 || fired) return;

            fired = true;
            ConstructionRequests.constructions.clear();

            // (94,110) is ~2.2 tiles from the standing Gateway: inside the
            // 2.5 proximity radius, but a free tile (nothing stands on it).
            APosition site = APosition.create(94, 110);

            Construction preparing = new Construction(AUnitType.Protoss_Gateway);
            preparing.setPositionToBuild(site);
            preparing.setStatus(ConstructionOrderStatus.PREPARING);

            assertFalse(
                ConstructionStatusChanger.shouldProximityFinish(preparing),
                "a builderless picked site next to a finished sibling is the "
                    + "normal street-grid shape - finishing it drops live "
                    + "demand and re-picks the same tile");

            Construction builderlessStarted = new Construction(AUnitType.Protoss_Gateway);
            builderlessStarted.setPositionToBuild(site);
            builderlessStarted.setStatus(ConstructionOrderStatus.NOT_STARTED);

            assertTrue(
                ConstructionStatusChanger.shouldProximityFinish(builderlessStarted),
                "builderless NOT_STARTED keeps the old path - nothing changes there");

            Construction heldPreparing = new Construction(AUnitType.Protoss_Gateway);
            heldPreparing.setPositionToBuild(site);
            heldPreparing.setStatus(ConstructionOrderStatus.PREPARING);
            heldPreparing.setBuilder(probe);

            assertTrue(
                ConstructionStatusChanger.shouldProximityFinish(heldPreparing),
                "a builder held on a PREPARING site stays visible to the old path");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }
}
