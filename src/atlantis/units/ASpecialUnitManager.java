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

        if (unit.isTank() && TerranTank.update(unit)) return true;
        else if (unit.is(AUnitType.Terran_Medic) && (new TerranMedic(unit)).update()) return true;
        else if (unit.is(AUnitType.Terran_Wraith) && (new TerranWraith(unit)).update()) return true;
        else if (unit.is(AUnitType.Terran_Ghost) && (new TerranGhost(unit)).update()) return true;

        // === Protoss ========================================

        if (unit.isProtoss() && unit.shields() <= 5 && unit.hpLessThan(34)) {
            if (unit.isMissionSparta()) {
                return false;
            }

            AUnit battery = Select.ourWithUnfinished(AUnitType.Protoss_Shield_Battery)
                .havingEnergy(40)
                .nearestTo(unit);
            if (
                battery != null && battery.distToMoreThan(unit, 6)
                && unit.move(battery, Actions.MOVE_SPECIAL, "ToBattery", false)
            ) {
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

        if (unit.isVulture() && TerranVulture.update(unit)) {
            return true;
        }
        else if (unit.is(AUnitType.Terran_Science_Vessel) && TerranScienceVessel.update(unit)) {
            return true;
        }
        else if (unit.isTerranInfantry() && TerranInfantry.update(unit)) {
            return true;
        }

        // === Protoss ========================================

        else if (unit.isObserver() && ProtossObserver.update(unit)) {
            return true;
        } else if (unit.is(AUnitType.Protoss_High_Templar) && ProtossHighTemplar.update(unit)) {
            return true;
        } else if (unit.isReaver() && ProtossReaver.update(unit)) {
            return true;
        }

        // === Zerg ========================================

        else if (unit.is(AUnitType.Zerg_Overlord) && ZergOverlordManager.update(unit)) {
            return true;
        }

        // =========================================================

        return false;
    }

}
