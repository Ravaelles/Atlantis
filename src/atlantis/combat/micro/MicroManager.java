package atlantis.combat.micro;

import atlantis.AtlantisGame;
import atlantis.combat.micro.terran.TerranMedic;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 * Default micro manager that will be used for all melee units.
 */
public class MicroManager extends AbstractMicroManager {

    @Override
    public boolean update(AUnit unit) {
        if (canIssueOrderToUnit(unit)) {
            unit.setTooltip("Last: " + unit.getLastUnitActionWasFramesAgo());

            // SPECIAL UNIT TYPE action
            if (handleSpecialUnit(unit)) {
                return true;
            }

            // =========================================================
            // Avoid dying because of very low hit points
            // @FIX: Breaks handleUnfavorableOdds(), units tend to avoid fighting too much
            
//            if (handleLowHealthIfNeeded(unit)) {
//                return true;
//            }
            
            // =========================================================
            // Avoid melee units
            if (handleAvoidCloseMeleeUnits(unit)) {
                return true;
            }

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
                unit.setTooltip("Attack");
                return true;
            }

            unit.setTooltip("Mission");

            // =========================================================
            // False: Did not use micro-manager, allow mission behavior.
            // True: Do not allow mission manager to handle this unit, because micro-manager issued command.
            boolean canGiveCommandToMissionManager = unit.getGroundWeaponCooldown() > 0;
            return canGiveCommandToMissionManager;
        } // =========================================================
        // Can't give orders to unit right now
        else {
            unit.setTooltip("ago: " + unit.getLastUnitActionWasFramesAgo());
            return true;
        }
    }

    // =========================================================
    /**
     * @return <b>true</b> if unit can be given order<br />
     * <b>false</b> if unit is in the shooting frame or does any other thing that mustn't be interrupted
     */
    private boolean canIssueOrderToUnit(AUnit unit) {
//        return true;
        return !(unit.isAttackFrame() || unit.isStartingAttack()); //replaces unit.isJustShooting();
    }

    /**
     * There are special units like Terran Marines, Zerg Overlords that should be following different behavior
     * than standard combat units.
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

}
