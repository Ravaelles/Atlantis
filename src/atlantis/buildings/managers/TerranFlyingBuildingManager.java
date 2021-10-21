package atlantis.buildings.managers;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import java.util.ArrayList;


public class TerranFlyingBuildingManager {

    private static final ArrayList<AUnit> flyingBuildings = new ArrayList<>();
    
    // =========================================================
    
    public static void update() {
        if (AGame.isPlayingAsTerran() && !AGame.isUms()) {
            if (shouldHaveAFlyingBuilding()) {
                liftABuildingAndFlyAmongStars();
            }

            updateIfBuildingNeedsToBeLifted();
            
            for (AUnit flyingBuilding : flyingBuildings) {
                updateFlyingBuilding(flyingBuilding);
            }
        }
    }

    // =========================================================

    private static void updateIfBuildingNeedsToBeLifted() {
        for (AUnit building : Select.ourBuildings().listUnits()) {
            if (building.isUnderAttack(1) && !building.isLifted() && building.hpPercent() < 28) {
                building.lift();
            }
        }
    }

    private static boolean updateFlyingBuilding(AUnit flyingBuilding) {
        APosition focusPoint = flyingBuildingFocusPoint();
        
        // Move towards focus point if needed
        if (focusPoint != null) {
            double distToFocusPoint = focusPoint.distanceTo(flyingBuilding);
            
            if (distToFocusPoint > 2) {
                flyingBuilding.move(focusPoint, UnitActions.MOVE, "Fly baby!");
            }
        }

        return false;
    }

    private static APosition flyingBuildingFocusPoint() {
        APosition containFocusPoint = Missions.globalMission().focusPoint();
        APosition attackFocusPoint = Missions.ATTACK.focusPoint();

        if (containFocusPoint != null && attackFocusPoint != null) {
            return containFocusPoint.translateTilesTowards(attackFocusPoint, 4);
        }

        return Select.ourTanks().first().getPosition();
    }

    // =========================================================

    private static boolean shouldHaveAFlyingBuilding() {
        if (!flyingBuildings.isEmpty()) {
            return false;
        }

        return Select.ourTanks().atLeast(1) || Select.countOurOfType(AUnitType.Terran_Vulture) >= 5;
    }

    private static void liftABuildingAndFlyAmongStars() {
        AUnit flying = Select.ourOfType(AUnitType.Terran_Barracks, AUnitType.Terran_Engineering_Bay).idle().first();
        if (flying != null) {
            flying.lift();
            flyingBuildings.add(flying);
        }
    }
    
    // =========================================================

    public static boolean isFlyingBuilding(AUnit unit) {
        return unit.type().isBuilding() && flyingBuildings.contains(unit);
    }
    
}
