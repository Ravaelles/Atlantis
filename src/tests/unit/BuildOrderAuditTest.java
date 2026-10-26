package tests.unit;

import atlantis.game.util.BuildOrderAudit;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.protoss.ProtossWarping;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;
import bwapi.Race;

/**
 * End-of-log build-order audit (owner): a BO building warping at a
 * different supply than planned names its order break out loud
 * (measured UMS 7th_rav: BO Core "At 16" warping at 25+ while Pylons
 * and Gateways jumped the queue). Dynamic warps carry no planned
 * supply and stay out.
 */
@Tag("production")
public class BuildOrderAuditTest extends AbstractTestWithWorld {

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
        BuildOrderAudit.clearForTests();
    }

    private void warpPylonAt(int supplyUsed, int minSupply, boolean buildOrder) {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 10) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setBuilder(probe);
            construction.setPositionToBuild(APosition.create(12, 10));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(construction);

            currentSupplyUsed = supplyUsed;
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, minSupply);
            if (buildOrder) order.markAsNotDynamic();
            construction.setProductionOrder(order);

            FakeUnit warped = fake(AUnitType.Protoss_Pylon, 12, 10);
            warped.setCompleted(false);
            ProtossWarping.updateNewBuildingJustWarped(warped);
        });
    }

    @Test
    public void lateWarpNamesTheOrderBreak() {
        BuildOrderAudit.clearForTests();
        warpPylonAt(26, 16, true);

        assertEquals(1, BuildOrderAudit.mismatchLines().size(),
            "one off-plan BO warp, one HIGH line, got: " + BuildOrderAudit.mismatchLines());
        assertTrue(BuildOrderAudit.mismatchLines().get(0).contains("At 16"),
            "the line names the planned supply: " + BuildOrderAudit.mismatchLines());
        assertTrue(BuildOrderAudit.mismatchLines().get(0).contains("26"),
            "the line names the actual supply: " + BuildOrderAudit.mismatchLines());
    }

    @Test
    public void onPlanWarpStaysSilent() {
        BuildOrderAudit.clearForTests();
        warpPylonAt(16, 16, true);

        assertTrue(BuildOrderAudit.mismatchLines().isEmpty(),
            "warping on plan is not an error: " + BuildOrderAudit.mismatchLines());
    }

    @Test
    public void dynamicWarpsStayOut() {
        BuildOrderAudit.clearForTests();
        warpPylonAt(29, 23, false);

        assertTrue(BuildOrderAudit.mismatchLines().isEmpty(),
            "dynamic demand has no planned supply to break: " + BuildOrderAudit.mismatchLines());
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }
}
