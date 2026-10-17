package tests.unit;

import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A facility id that leaves the scheduler must resolve back to its type at
 * the director and the registry: {@code producerTypeId()} speaks the short
 * display form ({@code name()}), so {@code getByName} must answer it.
 *
 * <p>
 * Measured need: V2 research never dispatched live - "CyberneticsC" (the
 * short name every research item carries) resolved to null, so
 * {@code resolveFacility} gave up before touching the Core ("facility busy
 * or missing"). Single-word facilities (Nexus, Gateway) round-tripped by
 * accident, which is why units flowed and research did not. Same trap
 * class as the one documented in {@code CompletedUnitsHistory}.
 * </p>
 */
public class FacilityNameRoundTripTest {

    @Test
    public void multiWordFacilitiesResolveThroughTheirShortName() {
        assertSame(AUnitType.Protoss_Cybernetics_Core,
            AUnitType.getByName(AUnitType.Protoss_Cybernetics_Core.name()));
        assertSame(AUnitType.Protoss_Templar_Archives,
            AUnitType.getByName(AUnitType.Protoss_Templar_Archives.name()));
        assertSame(AUnitType.Protoss_Robotics_Facility,
            AUnitType.getByName(AUnitType.Protoss_Robotics_Facility.name()));
        assertSame(AUnitType.Protoss_Photon_Cannon,
            AUnitType.getByName(AUnitType.Protoss_Photon_Cannon.name()));
        assertSame(AUnitType.Terran_Supply_Depot,
            AUnitType.getByName(AUnitType.Terran_Supply_Depot.name()));
    }

    @Test
    public void singleWordFacilitiesKeepResolving() {
        assertSame(AUnitType.Protoss_Nexus,
            AUnitType.getByName(AUnitType.Protoss_Nexus.name()));
        assertSame(AUnitType.Protoss_Gateway,
            AUnitType.getByName(AUnitType.Protoss_Gateway.name()));
    }
}
