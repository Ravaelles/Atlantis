package atlantis.combat.micro;

import atlantis.combat.micro.terran.TerranMedic;

import java.util.Collection;

import atlantis.AtlantisGame;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.util.PositionUtil;
import atlantis.util.UnitUtil;
import atlantis.wrappers.Select;
import atlantis.wrappers.Units;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

/**
 * Default micro manager that will be used for all melee units.
 */
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
//            if (handleNotExtremelyFavorableOdds(unit)) {
//                return true;
//            }

            // =========================================================
            // Don't spread too much
//            if (handleDontSpreadTooMuch(unit)) {
//                return true;
//            }

            // =========================================================
            // Attack enemy is possible
            if (AtlantisAttackEnemyUnit.handleAttackEnemyUnits(unit)) {
                return true;
            }
            
            // =========================================================
            // False: Did not use micro-manager, allow mission behavior
            // True: Do not allow mission manager to handle this unit
            boolean canGiveCommandToMissionManager = unit.getGroundWeaponCooldown() > 0;
            return canGiveCommandToMissionManager;
        } 

        // =========================================================
        // Can't give orders to unit right now
        else {
//            unit.setTooltip("x " + unit.getLastUnitActionWasFramesAgo());
            return true;
        }
    }

    // =========================================================
    
    /**
     * @return <b>true</b> if unit can be given order<br />
     * <b>false</b> if unit is in the shooting frame or does any other thing that mustn't be interrupted
     */
    private boolean canIssueOrderToUnit(Unit unit) {
        return !(unit.isAttackFrame() || unit.isStartingAttack()); //replaces unit.isJustShooting();
    }

    /**
     * There are special units like Terran Marines, Zerg Overlords that should be following different
     * behavior than standard combat units.
     */
    private boolean handleSpecialUnit(Unit unit) {
        
        // ZERG
        if (AtlantisGame.playsAsZerg()) {
            if (unit.getType().equals(UnitType.Zerg_Overlord)) {
                ZergOverlordManager.update(unit);
                return true;
            }
        }
        
        // TERRAN
        if (AtlantisGame.playsAsTerran()) {
            if (unit.getType().equals(UnitType.Terran_Medic)) {
                TerranMedic.update(unit);
                return true;
            }
        }
        
        return false;
    }

    /**
     * If e.g. Terran Marine stands too far forward, it makes him vulnerable. Make him go back.
     */
    private boolean handleDontSpreadTooMuch(Unit unit) {
    	Select<Unit> ourClose = (Select<Unit>) Select.ourCombatUnits().inRadius(7, unit.getPosition());
        Collection<Unit> ourForcesNearby = ourClose.exclude(unit).listUnits();
        Position goTo = null;
        if (ourForcesNearby.isEmpty()) {
            goTo = Select.ourCombatUnits().exclude(unit).first().getPosition();
        } else if (ourForcesNearby.size() <= 4) {
            goTo = UnitUtil.medianPosition(ourForcesNearby);
        }

        if (goTo != null && PositionUtil.distanceTo(unit.getPosition(), goTo) > 5) {
            unit.move(goTo);
            TooltipManager.setTooltip(unit, "Stand closer");
            //unit.setTooltip("Stand closer");
            return true;
        }

        return false;
    }

}
