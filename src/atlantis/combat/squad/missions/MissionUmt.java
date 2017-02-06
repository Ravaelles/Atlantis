package atlantis.combat.squad.missions;

import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.AtlantisMap;
import atlantis.information.UnitData;
import atlantis.scout.AtlantisScoutManager;
import static atlantis.scout.AtlantisScoutManager.getUmtFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.missions.UnitMissions;
import atlantis.wrappers.APosition;
import bwapi.Color;
import bwapi.Position;
import bwta.BaseLocation;
import bwta.Region;

/**
 * This is the mission that is best used in UMT maps.
 */
public class MissionUmt extends Mission {

    public MissionUmt(String name) {
        super(name);
    }

    // =========================================================
    @Override
    public boolean update(AUnit unit) {
        APosition focusPoint = getFocusPoint();
        unit.setTooltip("#MA");

        // Focus point is well known
        if (focusPoint != null) {
            if (unit.distanceTo(focusPoint) > 10 && !unit.isAttacking() && !unit.isMoving()) {
                unit.attack(focusPoint, UnitMissions.ATTACK_POSITION);
                unit.setTooltip("#MA:Concentrate!"); //unit.setTooltip("Mission focus");	//TODO: DEBUG
                return true;
            }
        } // =========================================================
        // Invalid focus point, no enemy can be found, scatter
        else {
            APosition position = AtlantisMap.getRandomInvisiblePosition(unit.getPosition());
            if (position != null) {
                unit.attack(position, UnitMissions.ATTACK_POSITION);
                Atlantis.getBwapi().drawLineMap(unit.getPosition(), position, Color.Red); //TODO DEBUG
                unit.setTooltip("#MA:Forward!");
                return true;
            }
        }

        unit.setTooltip("#MA:Nothing");

        return false;
    }

    // =========================================================
    /**
     * Returns the <b>position</b> (not the unit itself) where we should point our units to in hope because as
     * far as we know, the enemy is/can be there and it makes sense to attack in this region.
     */
    public static APosition getFocusPoint() {

        // === Define unit that will be center of our army =================
        AUnit flagshipUnit = Select.ourCombatUnits().first();
        if (flagshipUnit == null) {
            return null;
        }

        // === Return closest enemy ========================================
        AUnit nearestEnemy = Select.enemy().nearestTo(flagshipUnit);
        if (nearestEnemy != null) {
            return nearestEnemy.getPosition();
        }

        // === Return location to go to ====================================
        return AtlantisMap.getNearestUnexploredRegion(flagshipUnit.getPosition());
    }

}
