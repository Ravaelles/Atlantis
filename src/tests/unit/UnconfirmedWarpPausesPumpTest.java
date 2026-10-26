package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.protoss.prioritize.ProtossCriticalStuffInQueue;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;
import bwapi.Race;

/**
 * Warp-window pump calm (NEXT #83): an IN_PROGRESS construction with no
 * building entity yet is a warp the server may still be settling. Measured
 * on Wine as 100-mineral burn cycles (attempt spends, warp drops silently,
 * demote, retry spends again: bank 92-&gt;8-&gt;88-&gt;4 while capped) -
 * pausing the pump there protects the recovery bank. Linked warps and rich
 * banks are unaffected by design.
 */
@Tag("production")
public class UnconfirmedWarpPausesPumpTest extends AbstractTestWithWorld {

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
    public void unconfirmedWarpPausesPumpWhenPoor() {
        currentMinerals = 100;

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20)), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setPositionToBuild(APosition.create(24, 24));
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            ConstructionRequests.constructions.add(construction);

            assertFalse(
                ProtossCriticalStuffInQueue.hasEnoughMinerals(),
                "an IN_PROGRESS Pylon with no building linked may still be"
                    + " settling server-side - spending the bank now is the"
                    + " burn cycle");
        });
    }

    @Test
    public void linkedWarpDoesNotPausePump() {
        currentMinerals = 100;

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20)), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setPositionToBuild(APosition.create(24, 24));
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            construction.setBuildingUnit(fake(AUnitType.Protoss_Pylon, 24, 24));
            ConstructionRequests.constructions.add(construction);

            assertTrue(
                ProtossCriticalStuffInQueue.hasEnoughMinerals(),
                "a linked warp exists - nothing to protect");
        });
    }

    @Test
    public void richBankIgnoresUnconfirmedWarp() {
        currentMinerals = 300;

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20)), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setPositionToBuild(APosition.create(24, 24));
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            ConstructionRequests.constructions.add(construction);

            assertTrue(
                ProtossCriticalStuffInQueue.hasEnoughMinerals(),
                "a rich bank needs no protection - a retry is affordable");
        });
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }
}
