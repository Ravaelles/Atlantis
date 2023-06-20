package atlantis.combat.micro.avoid.special;

import atlantis.Atlantis;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.Bullet;
import bwapi.BulletType;

import java.util.List;


public class AvoidSpellsAndMines {

    public static boolean avoidSpellsAndMines(AUnit unit) {

        // === Psionic Storm ========================================
        
        if (unit.isUnderStorm()) {
            for (Bullet bullet : Atlantis.game().getBullets()) {

                // PSIONIC STORM
                if (bullet.getType().equals(BulletType.Psionic_Storm)) {
//                    System.err.println("------------- " + A.now() + " PSIONIC! ----------------");

                    if (handleMoveAwayIfCloserThan(unit, APosition.create(bullet.getPosition()), 1.5)) {
                        unit.setTooltipTactical("Psionic Storm!");
                        return true;
                    }
                }
            }
        }
        
        // === Mines ===============================================

        return handleMines(unit);
    }

    // =========================================================

    private static boolean handleMines(AUnit unit) {
        boolean canShootAtMines = unit.isRanged() && unit.canAttackGroundUnits();

        if (!canShootAtMines) {
            return false;
        }

        if (unit.enemiesNear().combatUnits().inRadius(6, unit).atLeast(3)) {
            return false;
        }

        int radius = Math.min(6, canShootAtMines ? unit.groundWeapon().maxRange() + 3 : 0);
        List<AUnit> mines = Select.enemies(AUnitType.Terran_Vulture_Spider_Mine).inRadius(radius, unit).list();
        for (AUnit mine : mines) {
            if (!mine.isVisibleUnitOnMap() || !mine.isAlive()) {
                continue;
            }

            // Our mine
            if (mine.isOur()) {
                if (mine.isMoving() && mine.distTo(unit) <= 3.5) {
                    unit.moveAwayFrom(mine.position(), 2, "Avoid mine!", Actions.MOVE_AVOID);
                    return true;
                }
            }

            // Enemy mine
            else {
                if (canShootAtMines) {
                    if (handleEnemyMineAsRangedUnit(unit, mine)) {
                        unit.setTooltipTactical("ShootMine");
                        return true;
                    }
                }
                else {
                    if (handleEnemyMineAsMeleeUnit(unit, mine)) {
                        unit.setTooltipTactical("AvoidMine");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean handleEnemyMineAsMeleeUnit(AUnit unit, AUnit mine) {
        unit.moveAwayFrom(mine.position(), 1, "Avoid mine!", Actions.MOVE_AVOID);
//        APainter.paintLine(unit, mine, Color.Yellow);
        return true;
    }

    private static boolean handleEnemyMineAsRangedUnit(AUnit unit, AUnit mine) {
//        if (mine.distTo(unit) <= 2.0) {
//            unit.runningManager().runFrom(mine, 3, Actions.MOVE_AVOID);
//            unit.setTooltipTactical("AVOID MINE(" + mine.distTo(unit) + ")");
//            return true;
//        }

        // Randomize fire if there are multiple mines
        if (unit.idIsOdd()) {
            AUnit otherMine = mine.friendsNear().ofType(AUnitType.Terran_Vulture_Spider_Mine).first();
            if (otherMine != null) {
                mine = otherMine;
            }
        }

        unit.attackUnit(mine);
        unit.setTooltipTactical("SHOOT MINE");
        return true;
    }

    private static boolean handleMoveAwayIfCloserThan(AUnit unit, APosition avoidCenter, double minDist) {
        if (unit.distTo(avoidCenter) < minDist) {
            unit.moveAwayFrom(avoidCenter, 3, "AvoidSpell", Actions.MOVE_AVOID);
            return true;
        }
        else {
            return false;
        }
    }
    
}
