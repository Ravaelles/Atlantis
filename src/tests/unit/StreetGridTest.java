package tests.unit;

import atlantis.placement.core.StreetGrid;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Street grid doctrine (owner order 2026-11-06, reviving the old
 * ProtossForbiddenByStreetGrid spacing): fixed 11x7 modulo tiles keep
 * corridors walkable. Pure tile math - zero per-frame cost.
 */
@Tag("placement")
public class StreetGridTest {

    @Test
    public void pylonTilesFollowTheStrictGrid() {
        assertTrue(StreetGrid.isStreetLegal(2, 2, AUnitType.Protoss_Pylon));
        assertTrue(StreetGrid.isStreetLegal(13, 9, AUnitType.Protoss_Pylon));
        assertTrue(StreetGrid.isStreetLegal(2, 4, AUnitType.Protoss_Pylon));

        assertFalse(StreetGrid.isStreetLegal(0, 0, AUnitType.Protoss_Pylon),
            "origin column stays a corridor");
        assertFalse(StreetGrid.isStreetLegal(3, 2, AUnitType.Protoss_Pylon),
            "only X%11==2 hosts 2x2");
        assertFalse(StreetGrid.isStreetLegal(2, 3, AUnitType.Protoss_Pylon),
            "Y%7==3 is a corridor for 2x2");
        assertFalse(StreetGrid.isStreetLegal(2, 5, AUnitType.Protoss_Pylon),
            "Y%7==5 is a corridor for 2x2");
    }

    @Test
    public void gatewayTilesFollowTheStandardGrid() {
        assertTrue(StreetGrid.isStreetLegal(2, 2, AUnitType.Protoss_Gateway));
        assertTrue(StreetGrid.isStreetLegal(4, 5, AUnitType.Protoss_Gateway));
        assertTrue(StreetGrid.isStreetLegal(6, 2, AUnitType.Protoss_Gateway));

        assertFalse(StreetGrid.isStreetLegal(8, 2, AUnitType.Protoss_Gateway),
            "X%11==8 is off-street for non-base buildings");
        assertFalse(StreetGrid.isStreetLegal(0, 2, AUnitType.Protoss_Gateway),
            "origin column stays a corridor");
        assertFalse(StreetGrid.isStreetLegal(4, 4, AUnitType.Protoss_Gateway),
            "Y%7==4 is a corridor outside 2x2");
    }

    @Test
    public void basesAndGasAreExempt() {
        assertTrue(StreetGrid.isStreetLegal(0, 0, AUnitType.Protoss_Nexus),
            "a Nexus lands on its base location whatever the modulo says");
        assertTrue(StreetGrid.isStreetLegal(3, 3, AUnitType.Protoss_Assimilator),
            "a refinery sits on its geyser, never on a street tile");
        assertTrue(StreetGrid.isStreetLegal(0, 0, null),
            "fail-open on unknown input");
    }
}
