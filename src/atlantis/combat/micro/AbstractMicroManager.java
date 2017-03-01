package atlantis.combat.micro;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.combat.ACombatEvaluator;
import atlantis.combat.squad.Squad;
import atlantis.position.APosition;
import atlantis.position.PositionOperationsWrapper;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.PositionUtil;
import bwapi.WeaponType;
import java.util.Collection;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public abstract class AbstractMicroManager {

    private static AUnit _nearestEnemyThatCanShootAtThisUnit = null;

    // =========================================================
    
//    public abstract boolean update(AUnit unit);

    // =========================================================
    /**
     * If chances to win the skirmish with the nearby enemy units aren't favorable, avoid fight and retreat.
     */
    protected static boolean handleUnfavorableOdds(AUnit unit) {
        boolean isNewFight = (unit.getUnitAction() != null && !unit.getUnitAction().isRunningOrRetreating());
        boolean isSituationFavorable = ACombatEvaluator.isSituationFavorable(unit, isNewFight);

        // If situation is unfavorable, retreat
//        if (!isSituationFavorable && !unit.isReadyToShoot() && (unit.canAnyCloseEnemyShootThisUnit()
        if (!isSituationFavorable && (unit.canAnyCloseEnemyShootThisUnit()
                || Select.enemy().combatUnits().inRadius(2.5, unit).count() > 0)) {
            unit.setTooltip("Retreat");
//            System.out.println("RETREAT " + unit);
            return unit.runFrom(null);
        }

        return false;
    }

    /**
     * If combat evaluator tells us that the potential skirmish with nearby enemies wouldn't result in
     * decisive victory either retreat or stand where you are.
     */
    protected boolean handleNotExtremelyFavorableOdds(AUnit unit) {
//        if (!AtlantisCombatEvaluator.isSituationExtremelyFavorable(unit)) {
//            if (isInShootRangeOfAnyEnemyUnit(unit)) {
////                unit.moveAwayFrom(_nearestEnemyThatCanShootAtThisUnit, 2);
////                return true;
//                return AtlantisRunManager.run(unit);
//            }
//        }

        return false;
    }

    /**
     * If unit is severly wounded, it should run.
     */
    protected boolean handleLowHealthIfNeeded(AUnit unit) {
        if (AGame.playsAsTerran()) {
            if (unit.getHP() <= 7) {
                AUnit rendezvousWithMedics = (AUnit) Select.ourBuildings().ofType(AtlantisConfig.BARRACKS).first();
                if (rendezvousWithMedics != null && rendezvousWithMedics.distanceTo(unit) > 5) {
                    unit.move(rendezvousWithMedics.getPosition(), UnitActions.HEAL);
                }
                return true;
            }
        }

        AUnit nearestEnemy = Select.nearestEnemy(unit.getPosition());
        if (nearestEnemy == null || PositionUtil.distanceTo(nearestEnemy, unit) > 6) {
            return false;
        }

        if (unit.getHitPoints() <= 16 || unit.getHPPercent() < 30) {
            if (Select.ourCombatUnits().inRadius(4, unit).count() <= 6) {
                return unit.getRunManager().run();
            }
        }

        return false;
    }

    /**
     * If e.g. Terran Marine stands too far forward, it makes him vulnerable. Make him go back.
     */
    protected boolean handleDontSpreadTooMuch(AUnit unit) {
        Squad squad = unit.getSquad();
        Select ourUnits = Select.from(squad.arrayList()).inRadius(10, unit);
        int ourUnitsNearby = ourUnits.count();
        int minUnitsNearby = (int) (squad.size() * 0.66);

        // =========================================================
        if (ourUnitsNearby < minUnitsNearby && ourUnitsNearby <= 3) {
            APosition goTo = PositionOperationsWrapper.averagePosition(ourUnits.list());
            if (goTo != null && goTo.distanceTo(unit) > 1) {
                unit.move(goTo, UnitActions.MOVE);
                unit.setTooltip("Closer");
                return true;
            }
        }

        return false;
    }

    /**
     * @return <b>true</b> if any of the enemy units in range can shoot at this unit.
     */
    protected boolean isInShootRangeOfAnyEnemyUnit(AUnit unit) {
        Collection<AUnit> enemiesInRange = (Collection<AUnit>) Select.enemy().combatUnits().inRadius(12, unit).listUnits();
        for (AUnit enemy : enemiesInRange) {
            WeaponType enemyWeapon = (unit.isAirUnit() ? enemy.getAirWeapon() : enemy.getGroundWeapon());
            double distToEnemy = PositionUtil.distanceTo(unit, enemy);

            // Compare against max range
            if (distToEnemy + 0.5 <= enemyWeapon.maxRange()) {
                _nearestEnemyThatCanShootAtThisUnit = enemy;
                return true;
            }

            // Compare against min range
//            if () {
//                distToEnemy >= enemyWeapon.getMinRange()
//                return true;
//            }
        }

        // =========================================================
        _nearestEnemyThatCanShootAtThisUnit = null;
        return false;
    }

}
