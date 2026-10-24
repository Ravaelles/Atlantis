package tests.unit;

import atlantis.game.A;
import atlantis.information.enemy.UnitsArchive;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
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
@Tag("infra")
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
        UnitsArchive.reset();
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

    /**
     * B134 cid60 shape (NEXT #150): live builder walked away from the site,
     * no warping entity ever appears, IN_PROGRESS sits 20k frames - the
     * 3.5-tile shield holds forever and no demote/twin ever fires. The
     * age-cap re-arms ancient entries (2400f > longest legal 1800f warp +
     * margin) while young travellers keep the shield (same far-away
     * fixture, frame 120 stays IN_PROGRESS).
     */
    private Construction walkedAway;
    private FakeUnit walkedProbe;

    @Test
    public void aWalkedAwayBuilderAgesOutAfter2400() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        walkedProbe = fake(AUnitType.Protoss_Probe, 30, 30);

        world(2600, units(nexus, walkedProbe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                walkedProbe.unitState().setLastPositionChanged(A.now());
                walkedAway = stalledCoreAt(walkedProbe, 12, 10);
            }

            if (A.now() == 120) {
                assertFalse(
                    ConstructionStatusChanger.demoteStalledInProgress(walkedAway, walkedProbe),
                    "young walked-away entry keeps the distance shield"
                        + " (travellers unaffected, 110f old)");
                assertEquals(
                    ConstructionOrderStatus.IN_PROGRESS, walkedAway.status(),
                    "no demote means no status change");
            }

            if (A.now() == 2500) {
                assertTrue(
                    ConstructionStatusChanger.demoteStalledInProgress(walkedAway, walkedProbe),
                    "ancient entry with no building re-arms to NOT_STARTED"
                        + " however far the live builder went (B134 cid60)");
                assertEquals(
                    ConstructionOrderStatus.NOT_STARTED, walkedAway.status());
            }
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

    @Test
    public void recentAttemptHoldsFire() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);

        world(130, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() != 120) return;

            // Same stalled shape that demotes (idle builder at site, no
            // building) - except the build order went out 0 frames ago.
            Construction construction = stalledCoreAt(probe, 12, 10);
            construction.setLastBuildAttemptNow();

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(construction, probe),
                "attempted this frame: the warp may still be settling"
                    + " server-side - demoting on the pre-attempt standstill"
                    + " clock kills fresh warps (measured: demote 7f after an"
                    + " accepted attempt)");
        });
    }

    /**
     * NEXT #155 (B142 oid25/cid122): the stall demote named cid only, so
     * the refusal series (order string), the demote and the warp never
     * joined mechanically. The row now names the order too.
     */
    @Test
    public void demoteTwinNamesOid() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);

        world(130, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                probe.unitState().setLastPositionChanged(A.now());
                standstill = stalledCoreAt(probe, 12, 10);
                standstill.setProductionOrder(
                    new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 22));
            }

            if (A.now() == 120) {
                EventLog.clearForTests();
                assertTrue(
                    ConstructionStatusChanger.demoteStalledInProgress(standstill, probe),
                    "fixture check: idle builder at an empty site demotes");
                String rows = EventLog.toJsonLines();
                assertTrue(
                    rows.contains("DEMOTE_STALLED")
                        && rows.contains("\"oid\":" + standstill.productionOrder().id()),
                    "the demote names the stalled order, got: " + rows);
            }
        });
    }

    private Construction archivedStalled;

    /**
     * The cid-331 shape (Wine macro 2026-10-21): the builder died
     * (UNIT_DIED twin fired, archive knows) but the wrapper still
     * reports hp &gt; 0, and the corpse lies far from the site. Pins
     * that {@code isDead} honors the archive - the link this demote
     * depends on: without it neither the dead-builder branch
     * (isDead lies) nor the standstill branch (dist &gt; 3.5)
     * ever fires and the warp freezes with zero twin rows.
     */
    @Test
    public void archivedDeadBuilderFarFromSiteDemotes() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);

        world(130, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                UnitsArchive.markUnitAsDestroyed(probe);
                archivedStalled = stalledCoreAt(probe, 12, 10);
            }

            if (A.now() == 120) {
                assertTrue(
                    probe.isAlive(),
                    "fixture check: stale hp still reads alive (else this"
                        + " pins the wrong shape)");
                assertTrue(
                    ConstructionStatusChanger.demoteStalledInProgress(archivedStalled, probe),
                    "archived-dead reads as dead despite stale hp and a far"
                        + " corpse: re-arm to NOT_STARTED so the retry path"
                        + " can reassign");
                assertEquals(
                    ConstructionOrderStatus.NOT_STARTED, archivedStalled.status());
            }
        });
    }

    @Test
    public void frozenWarpWithDeadBuilderTwinsOnce() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit deadProbe = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit liveProbe = fake(AUnitType.Protoss_Probe, 30, 30);
        FakeUnit warpingDead = fake(AUnitType.Protoss_Cybernetics_Core, 12, 10);
        warpingDead.setCompleted(false);
        FakeUnit warpingLive = fake(AUnitType.Protoss_Cybernetics_Core, 30, 30);
        warpingLive.setCompleted(false);

        world(2600, units(nexus, deadProbe, liveProbe, warpingDead, warpingLive), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                UnitsArchive.markUnitAsDestroyed(deadProbe);
                frozenStalled = stalledCoreAt(deadProbe, 12, 10);
                healthyStalled = stalledCoreAt(liveProbe, 30, 30);
            }

            if (A.now() == 2500) {
                assertTrue(
                    UnitsArchive.isDestroyed(deadProbe.id()),
                    "the archive must still know the frame-10 death");
                assertEquals(
                    ConstructionOrderStatus.IN_PROGRESS, frozenStalled.status(),
                    "the world must not have demoted it first (self-match holds)");
                assertFalse(
                    ConstructionStatusChanger.demoteStalledInProgress(frozenStalled, deadProbe),
                    "self-match still shields the demote - the twin names it, nothing moves");
                assertTrue(
                    frozenStalled.isFrozenWarpTwinned(),
                    "the twin fired once startedAgo crossed 2400 (the flag is the durable proof - the row itself may already have flushed to the log file, so the buffer is not the witness)");
                EventLog.clearForTests();
                ConstructionStatusChanger.demoteStalledInProgress(frozenStalled, deadProbe);
                assertFalse(
                    EventLog.toJsonLines().contains("FROZEN_WARP"),
                    "twins once per construction, not every frame");
                EventLog.clearForTests();
                ConstructionStatusChanger.demoteStalledInProgress(healthyStalled, liveProbe);
                assertFalse(
                    EventLog.toJsonLines().contains("FROZEN_WARP"),
                    "a live builder never twins, however old the warp");
            }
        });
    }

    private Construction frozenStalled;
    private Construction healthyStalled;
    private Construction standstill;
    private Construction builderless;

    @Test
    public void demoteTwinsCidTypeTileAndReasonOnBothPaths() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit live = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit dead = fake(AUnitType.Protoss_Probe, 30, 30);
        dead.setHp(0);

        world(130, units(nexus, live, dead), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                live.unitState().setLastPositionChanged(A.now());
                standstill = stalledCoreAt(live, 12, 10);
                builderless = stalledCoreAt(dead, 30, 30);
            }

            if (A.now() == 120) {
                EventLog.clearForTests();
                assertTrue(
                    ConstructionStatusChanger.demoteStalledInProgress(standstill, live),
                    "fixture check: idle builder at an empty site demotes");
                String stoodRows = EventLog.toJsonLines();
                assertTrue(
                    stoodRows.contains("DEMOTE_STALLED") && stoodRows.contains("stoodStillFor="),
                    "standstill demote twins cid/type/tile/reason, not just console: "
                        + stoodRows);

                EventLog.clearForTests();
                assertTrue(
                    ConstructionStatusChanger.demoteStalledInProgress(builderless, dead),
                    "fixture check: old builderless stall demotes");
                String goneRows = EventLog.toJsonLines();
                assertTrue(
                    goneRows.contains("DEMOTE_STALLED") && goneRows.contains("builder gone"),
                    "builder-gone demote twins cid/type/tile/reason, not just console: "
                        + goneRows);

                EventLog.clearForTests();
                Construction fresh = stalledCoreAt(live, 12, 10);
                fresh.setLastBuildAttemptNow();
                assertFalse(
                    ConstructionStatusChanger.demoteStalledInProgress(fresh, live),
                    "recent attempt holds fire");
                assertFalse(
                    EventLog.toJsonLines().contains("DEMOTE_STALLED"),
                    "a healthy flip twins nothing");
            }
        });
    }

    /**
     * NEXT #150, oid36 shape (Wine 182050: Pylon STARTED f6793, builder
     * alive, never tracked again, no building ever, no twin ever fires).
     * The age-cap cannot see whether the attached builder works THIS site:
     * a builder constructing elsewhere shields an ancient building-less
     * stall forever (only the stoodStill and builder-gone branches look at
     * the builder, and both miss a far-away constructing worker). Pinned
     * as-shielded (no behavior change): fixing this hole needs an owner
     * decision, never a silent flip.
     */
    private Construction busyStall;
    private FakeUnit busyProbe;

    @Test
    public void attachedBuilderConstructingElsewhereShieldsAncientStall() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        busyProbe = fake(AUnitType.Protoss_Probe, 30, 30);
        busyProbe.setConstructing(true);

        world(2600, units(nexus, busyProbe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                busyProbe.unitState().setLastPositionChanged(A.now());
                busyStall = stalledCoreAt(busyProbe, 12, 10);
            }

            if (A.now() == 2500) {
                assertFalse(
                    ConstructionStatusChanger.demoteStalledInProgress(busyStall, busyProbe),
                    "builder constructing elsewhere shields even a 2490f-old building-less stall"
                        + " (oid36 shape: attached, alive, untracked - named hole, not a fix)");
                assertEquals(
                    ConstructionOrderStatus.IN_PROGRESS, busyStall.status(),
                    "no demote means no status change");
            }
        });
    }

}
