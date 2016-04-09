package atlantis.combat.micro;

import atlantis.AtlantisGame;
import atlantis.combat.micro.terran.TerranMedic;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.PositionUtil;
import bwapi.Position;
import java.util.Collection;

/**
 * Default micro manager that will be used for all melee units.
 */
public class DefaultMeleeManager extends MicroMeleeManager {

    @Override
    public boolean update(AUnit unit) {
        if (canIssueOrderToUnit(unit)) {
            unit.setTooltip("Last: " + unit.getLastUnitActionWasFramesAgo());

            // SPECIAL UNIT TYPE action
            if (handleSpecialUnit(unit)) {
                return true;
            }

            // =========================================================
            // Check health status
            if (handleLowHealthIfNeeded(unit)) {
                return true;
            }

            // =========================================================
            // Check chances to win the fight
//            if (handleUnfavorableOdds(unit)) {
//                return true;
//            }
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
            // False: Did not use micro-manager, allow mission behavior.
            // True: Do not allow mission manager to handle this unit, because micro-manager issued command.
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
    private boolean canIssueOrderToUnit(AUnit unit) {
        return !(unit.isAttackFrame() || unit.isStartingAttack()); //replaces unit.isJustShooting();
    }

    /**
     * There are special units like Terran Marines, Zerg Overlords that should be following different
     * behavior than standard combat units.
     */
    private boolean handleSpecialUnit(AUnit unit) {
        
        // ZERG
        if (AtlantisGame.playsAsZerg()) {
            if (unit.isType(AUnitType.Zerg_Overlord)) {
                ZergOverlordManager.update(unit);
                return true;
            }
        }
        
        // TERRAN
        if (AtlantisGame.playsAsTerran()) {
            if (unit.isType(AUnitType.Terran_Medic)) {
                return TerranMedic.update(unit);
            }
        }
        
        return false;
    }

    /**
     * If e.g. Terran Marine stands too far forward, it makes him vulnerable. Make him go back.
     */
    private boolean handleDontSpreadTooMuch(AUnit unit) {
    	Select<AUnit> ourClose = (Select<AUnit>) Select.ourCombatUnits().inRadius(7, unit.getPosition());
        Collection<AUnit> ourForcesNearby = ourClose.exclude(unit).listUnits();
        Position goTo = null;
        if (ourForcesNearby.isEmpty()) {
            goTo = Select.ourCombatUnits().exclude(unit).first().getPosition();
        } else if (ourForcesNearby.size() <= 4) {
            goTo = PositionUtil.medianPosition(ourForcesNearby);
        }

        if (goTo != null && unit.distanceTo(goTo) > 5) {
            unit.move(goTo);
            TooltipManager.setTooltip(unit, "Stand closer");
            //unit.setTooltip("Stand closer");
            return true;
        }

        return false;
    }

}
