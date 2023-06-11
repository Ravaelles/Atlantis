package atlantis.combat;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.ASquadManager;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ACombatCommander {
    
    /**
     * Acts with all battle units.
     */
    public static void update() {

        // Global mission is de facto Alpha squad's mission
        Alpha.get().setMission(Missions.globalMission());

        ASquadManager.updateSquadTransfers();

        for (Squad squad : Squad.getSquads()) {
            handleSquad(squad);
        }
    }

    // =============================================================
    
    /**
     * Acts with all units that are part of given battle squad, according to the SquadMission object and using
     * proper micro managers.
     */
    private static void handleSquad(Squad squad) {

        // Act with every combat unit
        for (AUnit unit : squad.list()) {
            ACombatUnitManager.update(unit);

//            addInfoAboutNearestEnemyToTooltip(unit);
        }
    }

    // =========================================================

//    private static void addInfoAboutNearestEnemyToTooltip(AUnit unit) {
//        AUnit nearestEnemy = Select.enemyRealUnits().nearestTo(unit);
//        if (nearestEnemy != null) {
//            String tooltip = unit.tooltip() + "";
//            if (tooltip.contains(" / ")) {
//                unit.setTooltipTactical("");
//            }
//
//            double margin = AvoidEnemies.lowestSafetyMarginForAnyEnemy(unit);
//            unit.setTooltipTactical( (margin < 0 ? A.digit(margin) : "-") + " / " + unit.tooltip()
////            unit.setTooltipTactical( (margin < 9876 ? A.digit(margin) : "-") + " / " + unit.tooltip()
////                    A.digit(nearestEnemy.distanceTo(unit)) + " \\ "
////                            + A.digit(AAvoidUnits.lowestSafetyMarginForAnyEnemy(unit)) + " / "
//            );
//        }
//    }

}
