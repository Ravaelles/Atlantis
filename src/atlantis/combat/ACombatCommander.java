package atlantis.combat;

import atlantis.AGame;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.squad.Squad;
import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.A;
import atlantis.util.CodeProfiler;

public class ACombatCommander {
    
    /**
     * Acts with all battle units.
     */
    public static void update() {
        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_COMBAT);
        
        if (AGame.everyNthGameFrame(40)) {
            MissionChanger.evaluateGlobalMission();
        }
        
        // === Handle all squads ===================================
        
        for (Squad squad : Squad.getSquads()) {
            handleSquad(squad);
        }
        
        // =========================================================
        
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_COMBAT);
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
        for (AUnit unit : squad.arrayList()) {
            ACombatUnitManager.update(unit);

            addInfoAboutNearestEnemyToTooltip(unit);

            if (AGame.everyNthGameFrame(40)) {
                unit.lastX = unit.getX();
                unit.lastY = unit.getY();
            }
        }
    }

    // =========================================================

    private static void addInfoAboutNearestEnemyToTooltip(AUnit unit) {
        AUnit nearestEnemy = Select.enemyRealUnits().nearestTo(unit);
        if (nearestEnemy != null) {
            String tooltip = unit.getTooltip() + "";
            if (tooltip.contains(" < ")) {
                unit.setTooltip("");
            }
            unit.setTooltip(
                    A.digit(nearestEnemy.distanceTo(unit)) + " < "
                            + A.digit(AAvoidUnits.lowestSafetyMarginForAnyEnemy(unit)) + " / "
                            + unit.getTooltip()
            );
        }
    }

}
