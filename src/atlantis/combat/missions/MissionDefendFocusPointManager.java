package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;

public class MissionDefendFocusPointManager extends MissionFocusPointManager {

    @Override
    public APosition focusPoint() {
        if (AGame.isUms()) {
            return null;
        }

        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            return null;
        }

        // === Focus enemy attacking the main base =================

        AUnit nearEnemy = Select.enemy().combatUnits().nearestTo(mainBase);
        if (nearEnemy != null) {
            return nearEnemy.getPosition();
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