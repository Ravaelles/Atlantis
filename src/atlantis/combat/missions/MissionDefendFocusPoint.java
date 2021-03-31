package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwta.Chokepoint;

import static atlantis.scout.AScoutManager.getUmtFocusPoint;

public class MissionDefendFocusPoint {

    public static APosition focusPoint() {
        // === Handle UMT ==========================================

        if (AGame.isUmtMode()) {
            return null;
        }

        // === Focus enemy attacking the main base =================

        AUnit mainBase = Select.mainBase();
        if (mainBase != null) {
            AUnit nearEnemy = Select.enemy().combatUnits().nearestTo(mainBase);
            if (nearEnemy != null) {
                return nearEnemy.getPosition();
            }
        }

        // === Return position near the choke point ================

//        if (Select.ourBases().count() <= 1) {
//            return APosition.create(AtlantisMap.getChokepointForMainBase().getCenter());
//        }
//        else {
        Chokepoint chokepointForNaturalBase = AMap.getChokepointForNaturalBase();
        if (chokepointForNaturalBase != null) {
            return APosition.create(chokepointForNaturalBase.getCenter());
        }

        // === Return position near the first building ================

        AUnit building = Select.ourBuildings().first();
        if (building != null) {
            return APosition.create(AMap.getNearestChokepoint(building.getPosition()).getCenter());
        }

        return null;
    }

}