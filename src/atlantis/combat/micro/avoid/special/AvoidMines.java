package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import java.util.List;

public class AvoidMines extends Manager {
    public AvoidMines(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit() && !unit.isWorker();
    }

    @Override
    protected Manager handle() {
        if (handleMines()) return usedManager(this);

        return null;
    }

    protected boolean handleMines() {
        boolean canShootAtMines = unit.isRanged() && unit.canAttackGroundUnits();

        if (!canShootAtMines) return false;

        if (unit.enemiesNear().combatUnits().inRadius(6, unit).atLeast(3)) return false;

        int radius = Math.min(6, canShootAtMines ? unit.groundWeapon().maxRange() + 3 : 0);
        List<AUnit> mines = Select.enemies(AUnitType.Terran_Vulture_Spider_Mine).inRadius(radius, unit).list();
        for (AUnit mine : mines) {
            if (!mine.isVisibleUnitOnMap() || !mine.isAlive()) {
                continue;
            }

            // Our mine
            if (mine.isOur()) {
                if (mine.isMoving() && mine.distTo(unit) <= 3.5) {
                    unit.moveAwayFrom(mine.position(), 2, Actions.MOVE_AVOID, "Avoid mine!");
                    return true;
                }
            }

            // Enemy mine
            else {
                if (canShootAtMines) {
                    if (handleEnemyMineAsRangedUnit(mine)) {
                        unit.setTooltipTactical("ShootMine");
                        return true;
                    }
                }
                else {
                    if (handleEnemyMineAsMeleeUnit(mine)) {
                        unit.setTooltipTactical("AvoidMine");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // =========================================================

    private boolean handleEnemyMineAsMeleeUnit(AUnit mine) {
        unit.moveAwayFrom(mine.position(), 1, Actions.MOVE_AVOID, "Avoid mine!");
//        APainter.paintLine(mine, Color.Yellow);
        return true;
    }

    private boolean handleEnemyMineAsRangedUnit(AUnit mine) {
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
}
