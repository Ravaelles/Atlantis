package tests.unit;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An IN_PROGRESS construction whose warp never started must not hold its
 * worker forever.
 *
 * <p><b>The defect this pins (NEXT.md #70, measured on OpenBW
 * {@code 20261013_095545_short}):</b> a Cybernetics Core was accepted by the
 * engine (@4697, {@code engineAccepted=true} with the order-echo
 * {@code isConstructing=true}), the status flipped NOT_STARTED -&gt;
 * IN_PROGRESS on that echo, the engine then dropped the Build order (engine
 * order {@code PlayerGuard}, our {@code lastCommand=Build} aging) and no Core
 * unit ever existed. The worker stood at [87,111] for 2400 frames with zero
 * re-issue attempts: {@code BuilderManager} drives only NOT_STARTED while
 * {@code isBuilder} still claims the worker, and {@code CancelTooLong}
 * requires NOT_STARTED too - so the premature IN_PROGRESS was immune to every
 * recovery path.</p>
 *
 * <p>The fix demotes a stalled IN_PROGRESS (idle worker at the site, no
 * building of that type near it) back to NOT_STARTED, handing the existing
 * travel/issue/cancel/requeue machinery a live construction. The assertions
 * below pin the contract: demote the dead shape, keep every healthy one.</p>
 */
public class StalledInProgressDemoteTest extends AbstractTestWithWorld {

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
    public void anIdleBuilderAtAnEmptySiteDemotesToNotStarted() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);

        world(130, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                // Simulate a just-arrived worker: it moved this frame, so the
                // stall clock starts now (in a game the walk stamps it; the
                // stub never moves static fakes, so stamp it by hand).
                probe.unitState().setLastPositionChanged(A.now());
                Construction construction = stalledCoreAt(probe, 12, 10);

                assertFalse(
                    ConstructionStatusChanger.demoteStalledInProgress(construction, probe),
                    "a recently arrived worker must keep its IN_PROGRESS, not demote on the first frames"
                );
                assertEquals(
                    ConstructionOrderStatus.IN_PROGRESS, construction.status(),
                    "no demote means no status change"
                );
            }

            if (A.now() == 120) {
                Construction construction = stalledCoreAt(probe, 12, 10);

                assertTrue(
                    ConstructionStatusChanger.demoteStalledInProgress(construction, probe),
                    "an idle worker standing 110 frames at a site with no building must demote"
                );
                assertEquals(
                    ConstructionOrderStatus.NOT_STARTED, construction.status(),
                    "demoting hands the existing travel/issue/cancel/requeue machinery"
                        + " back a live construction"
                );
            }
        });
    }

    @Test
    public void aWarpingBuildingNearTheSiteKeepsInProgress() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit warpingCore = fake(AUnitType.Protoss_Cybernetics_Core, 12, 10);
        warpingCore.setCompleted(false);

        world(130, units(nexus, probe, warpingCore), new FakeUnit[0], () -> {
            if (A.now() != 120) return;

            Construction construction = stalledCoreAt(probe, 12, 10);

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(construction, probe),
                "a building going up at the site is progress, even if the worker idles next to it"
            );
        });
    }

    @Test
    public void aTravellingBuilderKeepsInProgress() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);
        probe.lastCommand = "Move";

        world(130, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() != 120) return;

            Construction construction = stalledCoreAt(probe, 12, 10);

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(construction, probe),
                "a worker still moving (here: far from the site) must not demote"
            );
        });
    }

    // =========================================================

    private Construction aging;
    private FakeUnit agingProbe;

    @Test
    public void aBuilderlessStallRearmsWhenOld() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        agingProbe = fake(AUnitType.Protoss_Probe, 12, 10);
        agingProbe.setHp(0);

        world(130, units(nexus, agingProbe), new FakeUnit[0], () -> {
            // Flip early: by frame 120 it is 110 frames old with no builder
            // and no building - the Wine shape (setStatus releases the Protoss
            // builder on the flip, so a refused echo leaves no one holding it).
            if (A.now() == 10) {
                aging = stalledCoreAt(agingProbe, 12, 10);
            }

            if (A.now() == 120) {
                assertTrue(
                    ConstructionStatusChanger.demoteStalledInProgress(aging, agingProbe),
                    "old, builderless and building-less: re-arm to NOT_STARTED so a new builder is assigned");
                assertEquals(
                    ConstructionOrderStatus.NOT_STARTED, aging.status());
            }
        });
    }

    @Test
    public void aFreshFlipIsGivenTime() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);
        probe.setHp(0);

        world(130, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() != 120) return;

            Construction construction = stalledCoreAt(probe, 12, 10);

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(construction, probe),
                "freshly flipped: a real warp shows its building within frames");
        });
    }

    // =========================================================

    private Construction stalledCoreAt(FakeUnit probe, int tx, int ty) {
        Construction construction = new Construction(AUnitType.Protoss_Cybernetics_Core);
        construction.setBuilder(probe);
        construction.setPositionToBuild(APosition.create(tx, ty));
        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        return construction;
    }
}
