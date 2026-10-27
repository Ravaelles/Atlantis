package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.ProtossForbiddenByStreetGrid;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-15 (NEXT #81): the corner-base Pylon hunt measured the
 * street grid refusing ~30% of candidates (`PTX modulo is 0` + `PTY
 * modulo is 0`) while supply was merely tight (free 6-7), with the escape
 * hatches opening only at free 0 (or free<=5 banked and isolated). This
 * pins the doctrine as measured, so any relaxation changes numbers here
 * first instead of in a Wine game: grid holds normally, both hatches
 * open, and the free-6 boundary stays shut.
 */
@Tag("infra")
public class StreetGridDoctrineTest extends AbstractTestWithWorld {

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

    @Test
    public void gridHoldsComfortableAndOpensBothHatches() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 11),
            fake(AUnitType.Protoss_Probe, 12, 12)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            AUnit builder = Select.ourWorkers().first();
            // tx 44 sits on the 11-grid (44 % 11 == 0), far from the base
            // so the isolation clause can engage.
            APosition gridTile = APosition.create(44, 40);

            currentSupplyUsed = 10;
            currentSupplyTotal = 25;
            currentMinerals = 400;
            assertTrue(
                ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
                    builder, AUnitType.Protoss_Pylon, gridTile),
                "comfortable supply: the street grid holds its corridors");

            currentSupplyUsed = 25;
            assertFalse(
                ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
                    builder, AUnitType.Protoss_Pylon, gridTile),
                "supply blocked: aesthetics yield, the Pylon goes anywhere buildable");

            currentSupplyUsed = 20;
            assertFalse(
                ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
                    builder, AUnitType.Protoss_Pylon, gridTile),
                "free 5 banked and isolated: the second hatch opens");

            currentSupplyUsed = 19;
            assertTrue(
                ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
                    builder, AUnitType.Protoss_Pylon, gridTile),
                "free 6: both hatches shut - this is the Wine pain boundary");
        });
    }
}
