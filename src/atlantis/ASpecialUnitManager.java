package atlantis;

import atlantis.combat.micro.terran.*;
import atlantis.combat.micro.transport.ATransportManager;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.dedicated.protoss.ProtossHighTemplar;
import atlantis.dedicated.protoss.ProtossObserver;
import atlantis.dedicated.protoss.ProtossReaver;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.Us;

public class ASpecialUnitManager {

    /**
     * There are some units that should have individual micro managers like Zerg Overlord. If unit is "dedicated unit"
     * it will use its own manager and return true, meaning no other managers should be used.
     *
     * Returning false allows standard micro managers to be used.
     */
    public static boolean handledUsingDedicatedUnitManager(AUnit unit) {

        if (unit.type().isTransportExcludeOverlords() && ATransportManager.handle(unit)) {
            return true;
        }

        // === Terran ========================================

        if (Us.isTerran() && TerranCloakableManager.update(unit)) {
            return true;
        }

        if (unit.type().isTank()) {
            return TerranTank.update(unit);
        } else if (unit.type().isVulture()) {
            return TerranVulture.update(unit);
        } else if (unit.type().isTerranInfantry()) {
            return TerranInfantry.update(unit);
        } else if (unit.isType(AUnitType.Terran_Medic)) {
            return TerranMedic.update(unit);
        }

        // === Protoss ========================================

        else if (unit.is(AUnitType.Protoss_Observer) && ProtossObserver.update(unit)) {
            return true;
        } else if (unit.is(AUnitType.Protoss_High_Templar) && ProtossHighTemplar.update(unit)) {
            return true;
        } else if (unit.is(AUnitType.Protoss_Reaver) && ProtossReaver.update(unit)) {
            return true;
        }

        // === Zerg ========================================

        else if (unit.is(AUnitType.Zerg_Overlord)) {
            return ZergOverlordManager.update(unit);
        }

        // =========================================================

        return false;
    }
}
