package atlantis;

import atlantis.combat.micro.terran.TerranInfantryManager;
import atlantis.combat.micro.terran.TerranMedic;
import atlantis.combat.micro.terran.TerranSiegeTankManager;
import atlantis.combat.micro.terran.TerranVultureManager;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.special.ProtossObserverManager;
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
            if (unit.getType().isSiegeTank()) {
                return TerranSiegeTankManager.update(unit);
            } else if (unit.getType().isVulture()) {
                return TerranVultureManager.update(unit);
            } else if (unit.getType().isTerranInfantry()) {
                return TerranInfantryManager.update(unit);
            } else if (unit.isType(AUnitType.Terran_Medic)) {
                return TerranMedic.update(unit);
            }
        }

        // === Zerg ========================================

        else if (AGame.isPlayingAsProtoss()) {
            if (unit.getType().equals(AUnitType.Protoss_Observer) && ProtossObserverManager.update(unit)) {
                return true;
            } else if (unit.getType().equals(AUnitType.Protoss_Reaver) && ProtossObserverManager.update(unit)) {
                return true;
            }
        }

        // === Zerg ========================================

        else if (AGame.isPlayingAsZerg()) {
            if (unit.getType().equals(AUnitType.Zerg_Overlord)) {
                ZergOverlordManager.update(unit);
                return true;
            }
        }

        // =========================================================

        return false;
    }
}
