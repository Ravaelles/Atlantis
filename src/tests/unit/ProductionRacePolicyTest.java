package tests.unit;

import atlantis.production.v2.ProductionRacePolicy;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #8 pilot: the five type identities per race (ADR 0007), pinned before
 * any selector is swapped to the port.
 */
public class ProductionRacePolicyTest {

    @Test
    public void protossIdentities() {
        ProductionRacePolicy policy = ProductionRacePolicy.forRace("Protoss");
        assertEquals(AUnitType.Protoss_Probe, policy.workerType());
        assertEquals(AUnitType.Protoss_Zealot, policy.armyUnitType());
        assertEquals(AUnitType.Protoss_Nexus, policy.baseType());
        assertEquals(AUnitType.Protoss_Pylon, policy.supplyProviderType());
        assertEquals(AUnitType.Protoss_Assimilator, policy.gasBuildingType());
    }

    @Test
    public void terranIdentities() {
        ProductionRacePolicy policy = ProductionRacePolicy.forRace("Terran");
        assertEquals(AUnitType.Terran_SCV, policy.workerType());
        assertEquals(AUnitType.Terran_Marine, policy.armyUnitType());
        assertEquals(AUnitType.Terran_Command_Center, policy.baseType());
        assertEquals(AUnitType.Terran_Supply_Depot, policy.supplyProviderType());
        assertEquals(AUnitType.Terran_Refinery, policy.gasBuildingType());
    }

    @Test
    public void zergIdentities() {
        ProductionRacePolicy policy = ProductionRacePolicy.forRace("Zerg");
        assertEquals(AUnitType.Zerg_Drone, policy.workerType());
        assertEquals(AUnitType.Zerg_Zergling, policy.armyUnitType());
        assertEquals(AUnitType.Zerg_Hatchery, policy.baseType());
        assertEquals(AUnitType.Zerg_Overlord, policy.supplyProviderType());
        assertEquals(AUnitType.Zerg_Extractor, policy.gasBuildingType());
    }

    @Test
    public void selectionIsCaseInsensitiveAndSingleSite() {
        assertTrue(ProductionRacePolicy.forRace("protoss") instanceof atlantis.production.v2.ProtossProductionRacePolicy);
        assertTrue(ProductionRacePolicy.forRace("TERRAN") instanceof atlantis.production.v2.TerranProductionRacePolicy);
        assertTrue(ProductionRacePolicy.forRace("Zerg") instanceof atlantis.production.v2.ZergProductionRacePolicy);
    }

    @Test
    public void unknownRaceFailsFast() {
        assertThrows(IllegalArgumentException.class, () -> ProductionRacePolicy.forRace("Ums"));
        assertThrows(IllegalArgumentException.class, () -> ProductionRacePolicy.forRace(null));
    }
}
