package atlantis.combat.micro.avoid.special;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import java.util.List;

public class AvoidMines {
    protected  boolean handleMines() {
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
                if (mine.isMoving() && mine.distTo() <= 3.5) {
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

    // =========================================================

    private  boolean handleEnemyMineAsMeleeUnit(AUnit mine) {
        unit.moveAwayFrom(mine.position(), 1, "Avoid mine!", Actions.MOVE_AVOID);
//        APainter.paintLine(unit, mine, Color.Yellow);
        return true;
    }

    private  boolean handleEnemyMineAsRangedUnit(AUnit mine) {
//        if (mine.distTo() <= 2.0) {
//            unit.runningManager().runFrom(mine, 3, Actions.MOVE_AVOID);
//            unit.setTooltipTactical("AVOID MINE(" + mine.distTo() + ")");
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
}