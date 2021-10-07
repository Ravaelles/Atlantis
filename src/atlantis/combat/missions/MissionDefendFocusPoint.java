package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwta.Chokepoint;

import static atlantis.scout.AScoutManager.getUmtFocusPoint;

public class MissionDefendFocusPoint {

    public static APosition focusPoint() {
        if (AGame.isUmtMode()) {
            return null;
        }

        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            return null;
        }

        // === Focus enemy attacking the main base =================

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
        AChokepoint chokepointForNaturalBase = AMap.getChokepointForNaturalBase(mainBase.getPosition());
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