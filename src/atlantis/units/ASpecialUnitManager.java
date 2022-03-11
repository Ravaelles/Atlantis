package atlantis.units;

import atlantis.combat.micro.terran.*;
import atlantis.combat.micro.transport.ATransportManager;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.protoss.ProtossHighTemplar;
import atlantis.protoss.ProtossObserver;
import atlantis.protoss.ProtossReaver;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ASpecialUnitManager {

    /**
     * BLOCK other managers from taking precedence in action sequence.
     *
     * Returning false allows standard micro managers to be used.
     */
    public static boolean updateAndOverrideAllOtherManagers(AUnit unit) {

        // === Terran ========================================

        if (We.terran() && TerranCloakableManager.update(unit)) {
            return true;
        }

        if (unit.isTank()) {
            return TerranTank.update(unit);
        }
        else if (unit.is(AUnitType.Terran_Medic)) {
            return TerranMedic.update(unit);
        }
        else if (unit.is(AUnitType.Terran_Ghost)) {
            return TerranMedic.update(unit);
        }

        // === Protoss ========================================

        if (unit.isProtoss() && unit.shields() <= 5 && unit.hpLessThan(34)) {
            if (unit.isMissionSparta()) {
                return false;
            }

            AUnit battery = Select.ourOfTypeIncludingUnfinished(AUnitType.Protoss_Shield_Battery)
                .havingEnergy(40)
                .nearestTo(unit);
            if (battery != null && battery.distToMoreThan(unit, 6)) {
                unit.move(battery, Actions.MOVE_SPECIAL, "ToBattery", false);
                return true;
            }
        }

        // === Zerg ========================================

        // =========================================================

        return false;
    }

    /**
     * Does NOT block other managers from taking precedence.
     */
    public static boolean updateAndAllowTopManagers(AUnit unit) {

        if (unit.type().isTransportExcludeOverlords() && ATransportManager.handleTransportUnit(unit)) {
            return true;
        }

        // === Terran ========================================

        if (We.terran() && TerranCloakableManager.update(unit)) {
            return true;
        }

        if (unit.isVulture()) {
            return TerranVulture.update(unit);
        } else if (unit.is(AUnitType.Terran_Science_Vessel)) {
            return TerranScienceVessel.update(unit);
        }

        else if (unit.isTerranInfantry()) {
            return TerranInfantry.update(unit);
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
