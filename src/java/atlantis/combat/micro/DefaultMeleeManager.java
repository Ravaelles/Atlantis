package atlantis.combat.micro;

import atlantis.combat.micro.terran.TerranMedic;
import atlantis.AtlantisGame;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.wrappers.SelectUnits;
import atlantis.wrappers.Units;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class DefaultMeleeManager extends MicroMeleeManager {

    @Override
    public boolean update(Unit unit) {
        if (canIssueOrderToUnit(unit)) {
//            unit.setTooltip("Start " + unit.getLastUnitActionWasFramesAgo());

            // SPECIAL UNIT TYPE action
            if (handleSpecialUnit(unit)) {
                return true;
            }

            // =========================================================
            // Check health status
//            if (handleLowHealthIfNeeded(unit)) {
//                return true;
//            }

            // =========================================================
            // Check chances to win the fight
            if (handleUnfavorableOdds(unit)) {
                return true;
            }
            if (handleNotExtremelyFavorableOdds(unit)) {
                return true;
            }

            // =========================================================
            // Don't spread too much
//            if (handleDontSpreadTooMuch(unit)) {
//                return true;
//            }

            // =========================================================
            // Attack enemy is possible
            return AtlantisAttackEnemyUnit.handleAttackEnemyUnits(unit);
        } 

        // =========================================================
        // Can't give orders to unit right now
        else {
//            unit.setTooltip("x " + unit.getLastUnitActionWasFramesAgo());
            return true;
        }
    }

    // =========================================================
    
    private boolean canIssueOrderToUnit(Unit unit) {
        return !unit.isJustShooting();
    }

    private boolean handleSpecialUnit(Unit unit) {
        
        // ZERG
        if (AtlantisGame.playsAsZerg()) {
            if (unit.isType(UnitTypes.Zerg_Overlord)) {
                ZergOverlordManager.update(unit);
                return true;
            }
        }
        
        // TERRAN
        if (AtlantisGame.playsAsTerran()) {
            if (unit.isType(UnitTypes.Terran_Medic)) {
                TerranMedic.update(unit);
                return true;
            }
        }
        
        return false;
    }

    private boolean handleDontSpreadTooMuch(Unit unit) {
        Units ourForcesNearby = SelectUnits.ourCombatUnits().inRadius(7, unit).exclude(unit).units();
        Position goTo = null;
        if (ourForcesNearby.isEmpty()) {
            goTo = SelectUnits.ourCombatUnits().exclude(unit).first();
        } else if (ourForcesNearby.size() <= 4) {
            goTo = ourForcesNearby.positionMedian();
        }

        if (goTo != null && unit.distanceTo(goTo) > 5) {
            unit.move(goTo);
            unit.setTooltip("Stand closer");
            return true;
        }

        return false;
    }

}
