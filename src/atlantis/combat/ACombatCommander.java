package atlantis.combat;

import atlantis.AGame;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.squad.Squad;
import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.A;
import atlantis.util.CodeProfiler;

public class ACombatCommander {
    
    /**
     * Acts with all battle units.
     */
    public static void update() {
        if (AGame.everyNthGameFrame(40)) {
            MissionChanger.evaluateGlobalMission();
        }
        
        // === Handle all squads ===================================
        
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
        squad.setMission(Missions.globalMission());

        // =========================================================
        
        // Act with every combat unit
        for (AUnit unit : squad.list()) {
            ACombatUnitManager.update(unit);

            addInfoAboutNearestEnemyToTooltip(unit);
        }
    }

    // =========================================================

    private static void addInfoAboutNearestEnemyToTooltip(AUnit unit) {
        AUnit nearestEnemy = Select.enemyRealUnits().nearestTo(unit);
        if (nearestEnemy != null) {
            String tooltip = unit.getTooltip() + "";
            if (tooltip.contains(" / ")) {
                unit.setTooltip("");
            }

            double margin = AAvoidUnits.lowestSafetyMarginForAnyEnemy(unit);
            unit.setTooltip( (margin < 999 ? A.digit(margin) : "-") + " / " + unit.getTooltip()
//                    A.digit(nearestEnemy.distanceTo(unit)) + " \\ "
//                            + A.digit(AAvoidUnits.lowestSafetyMarginForAnyEnemy(unit)) + " / "
            );
        }
    }

}
