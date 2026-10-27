package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.protoss.ProtossWarping;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A construction whose building appears somewhere OTHER than the reserved tile
 * must not
 * keep its builder forever.
 *
 * <p>
 * <b>The defect this pins (NEXT.md #53, measured from
 * {@code out/openbw/bot.log}):</b> a
 * Probe sits 1.3 tiles from its Gateway, all preconditions true
 * ({@code buildable=true}, {@code canAfford=true},
 * {@code exploredOnFootprint=12/12}), the
 * build order has been issued - and {@code isConstructing=false},
 * {@code status=NOT_STARTED}
 * for 3534 frames, because the engine never filled the field the status used to
 * depend on.
 * Meanwhile the same run builds a building of that type and the construction
 * that
 * commissioned it is still waiting.
 * </p>
 *
 * <p>
 * The reason is that the tile the construction reserved and the tile the worker
 * actually
 * built on can differ ({@code RefreshConstructionPosition} moves the site; the
 * engine
 * snaps a footprint; a blocked builder re-finds a place).
 * {@code ProtossWarping} is the
 * only code that finishes a construction, and it identifies the construction
 * <b>by
 * position</b> ({@code DefineConstructionForNewUnit.defineConstruction}, radius
 * 2.5). When
 * the building lands outside that radius the lookup returns {@code null}, the
 * construction
 * is never marked {@code IN_PROGRESS}, its builder is never released to mining,
 * and both
 * are stuck for the rest of the game - one worker fewer, permanently, each
 * time.
 * </p>
 *
 * <p>
 * The assertions are about the CONTRACT, not about engine physics: whatever the
 * status and
 * the tile, a worker whose construction produced a finished unit of the right
 * type must be
 * given back. The stub world cannot arbitrate "the building really landed
 * here", so the
 * test states the rule the fix has to satisfy and holds it to it.
 * </p>
 */
@Tag("production")
public class OrphanedConstructionTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void aBuildingOnItsReservedTileIsFinishedNotJustMarkedInProgress() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setBuilder(probe);
            construction.setPositionToBuild(APosition.create(12, 10));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(construction);

            // The Pylon really did land on the reserved tile: the positional lookup finds it.
            FakeUnit onItsTile = fake(AUnitType.Protoss_Pylon, 12, 10);

            ProtossWarping.updateNewBuildingJustWarped(onItsTile);

            assertFalse(ConstructionRequests.constructions.contains(construction), "a construction whose building appeared must be FINISHED and removed -"
                + " IN_PROGRESS leaves it counted as an active construction forever,"
                + " which is the same leak the orphan case has");
            assertNull(construction.builder(), "and its worker released to mining");
        });
    }

    @Test
    public void aBuildingThatAppearedAwayFromTheReservedTileStillFreesItsBuilder() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            // The construction reserved [12,10] ...
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setBuilder(probe);
            construction.setPositionToBuild(APosition.create(12, 10));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(construction);

            // ... and the worker put the Pylon down eight tiles away, which is what
            // `RefreshConstructionPosition` drift and the engine's footprint snapping
            // produce in a real game.
            FakeUnit builtElsewhere = fake(AUnitType.Protoss_Pylon, 20);

            ProtossWarping.updateNewBuildingJustWarped(builtElsewhere);

            // 8 tiles apart: outside `DefineConstructionForNewUnit`'s 2.5-tile radius, which is
            // the real case (the finder moves a site, the engine snaps a footprint).
            assertEquals(8.0, construction.buildPosition().distTo(builtElsewhere), 0.01,
                "the fixture must really be outside the positional lookup radius");

            assertFalse(ConstructionRequests.constructions.contains(construction), "a construction whose building appeared elsewhere must be finished and removed,"
                + " not left in the queue counting as an active building forever");
            assertNull(
                construction.builder(),
                "and it must release the worker it claimed, so the worker can mine again -"
                    + " the measured duplicate-worker deadlock of NEXT.md #53");
        });
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }
}
