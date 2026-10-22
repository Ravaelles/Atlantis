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
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A site its own finished building stands on is dead, not waiting.
 *
 * <p><b>Measured (Wine):</b> a CyberneticsC construction survived its own
 * warped building standing on its exact tiles - builder held 369 frames,
 * {@code canBuild000=false}, bank 600 - shielded by the unworked-adoption
 * exemption, which exists for sites that may still build. Exact tiles
 * (same type means same footprint) split the two: a grazing sibling stays
 * exempt, an unfinished (warping) twin waits conservatively.</p>
 */
@Tag("placement")
public class StaleDuplicateFinishesOnItsTilesTest extends AbstractTestWithWorld {

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

    private Construction adoptedAt(APosition site, FakeUnit builder) {
        Construction construction = new Construction(AUnitType.Protoss_Cybernetics_Core);
        construction.setPositionToBuild(site);
        construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
        construction.setBuilder(builder);

        return construction;
    }

    @Test
    public void ownFinishedBuildingOnSiteFinishes() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 90, 110);

        world(5, units(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Cybernetics_Core, 94, 110),
            builder
        ), new FakeUnit[0], () -> {
            if (A.now() != 3 || fired) return;

            fired = true;

            APosition site = APosition.create(94, 110);
            assertTrue(
                ConstructionStatusChanger.shouldProximityFinish(adoptedAt(site, builder)),
                "own finished Core on the reserved tiles: the site can never "
                    + "build, no exemption shields it");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void nearSiblingAndWarpingOverlapWait() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 90, 110);

        world(5, units(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Pylon, 96, 111),
            builder
        ), new FakeUnit[0], () -> {
            if (A.now() != 3 || fired) return;

            fired = true;

            // (94,110) is ~2.2 from the standing Pylon: inside proximity
            // but off its 2x2 footprint - the churn case stays exempt.
            Construction sibling = new Construction(AUnitType.Protoss_Pylon);
            sibling.setPositionToBuild(APosition.create(94, 110));
            sibling.setStatus(ConstructionOrderStatus.NOT_STARTED);
            sibling.setBuilder(builder);
            assertFalse(
                ConstructionStatusChanger.shouldProximityFinish(sibling),
                "nearby sibling is not coverage - live demand survives");

            // Warping (unfinished) overlap: a dropped warp must not
            // silently eat demand.
            FakeUnit warping = fake(AUnitType.Protoss_Cybernetics_Core, 94, 110);
            warping.setCompleted(false);
            Construction covered = adoptedAt(APosition.create(94, 110), builder);
            assertFalse(
                ConstructionStatusChanger.shouldProximityFinish(covered),
                "unfinished overlap waits conservatively");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void adjacentNonOverlappingSiteSurvives() {
        world(5, units(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Pylon, 118, 16),
            fake(AUnitType.Protoss_Probe, 90, 110)
        ), new FakeUnit[0], () -> {
            if (A.now() != 3 || fired) return;

            fired = true;

            // Measured (OpenBW): a fresh [119,18] Pylon construction next to
            // the warped [118,16] one was proximity-finished and silently
            // removed (centers 2.2 apart, footprints disjoint), then
            // re-requested on the same tile 5x in a minute - totalOfType
            // pinned at 1, no cancel twin anywhere. Adjacent is packed,
            // not covered.
            Construction fresh = new Construction(AUnitType.Protoss_Pylon);
            fresh.setPositionToBuild(APosition.create(119, 18));
            fresh.setStatus(ConstructionOrderStatus.NOT_STARTED);
            assertFalse(
                ConstructionStatusChanger.shouldProximityFinish(fresh),
                "adjacent non-overlapping site is live demand, not a duplicate");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void overlappingSiteStillFinishes() {
        world(5, units(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Pylon, 118, 16),
            fake(AUnitType.Protoss_Probe, 90, 110)
        ), new FakeUnit[0], () -> {
            if (A.now() != 3 || fired) return;

            fired = true;

            // [118,17] shares the y=17 row with the warped [118,16]
            // footprint: an engine-shifted twin, still covered.
            Construction shifted = new Construction(AUnitType.Protoss_Pylon);
            shifted.setPositionToBuild(APosition.create(118, 17));
            shifted.setStatus(ConstructionOrderStatus.NOT_STARTED);
            assertTrue(
                ConstructionStatusChanger.shouldProximityFinish(shifted),
                "overlapping footprints mean the demand already stands");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }
}
