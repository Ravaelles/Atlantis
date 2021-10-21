package atlantis;

import atlantis.combat.micro.terran.*;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.special.protoss.ProtossObserver;
import atlantis.special.protoss.ProtossReaver;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ASpecialUnitManager {

    /**
     * There are some units that should have individual micro managers like Zerg Overlord. If unit is "dedicated unit"
     * it will use its own manager and return true, meaning no other managers should be used.
     *
     * Returning false allows standard micro managers to be used.
     */
    public static boolean handledUsingDedicatedUnitManager(AUnit unit) {
        // === Terran ========================================

        if (AGame.isPlayingAsTerran()) {
            if (TerranCloakableManager.update(unit)) {
                return true;
            }

            if (unit.type().isSiegeTank()) {
                return TerranTank.update(unit);
            } else if (unit.type().isVulture()) {
                return TerranVulture.update(unit);
            } else if (unit.type().isTerranInfantry()) {
                return TerranInfantry.update(unit);
            } else if (unit.isType(AUnitType.Terran_Medic)) {
                return TerranMedic.update(unit);
            }
        }

        // === Protoss ========================================

        else if (AGame.isPlayingAsProtoss()) {
            if (ProtossShieldBattery.handle(unit)) {
                return true;
            } else if (unit.is(AUnitType.Protoss_Observer) && ProtossObserver.update(unit)) {
                return true;
            } else if (unit.is(AUnitType.Protoss_High_Templar) && ProtossObserver.update(unit)) {
                return true;
            } else return unit.is(AUnitType.Protoss_Reaver) && ProtossReaver.update(unit);
        }

        // === Zerg ========================================

        else if (AGame.isPlayingAsZerg()) {
            if (unit.is(AUnitType.Zerg_Overlord)) {
                ZergOverlordManager.update(unit);
                return true;
            }
        }

        // =========================================================

        return false;
    }
}
