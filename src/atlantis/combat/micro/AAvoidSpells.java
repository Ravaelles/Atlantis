package atlantis.combat.micro;

import atlantis.Atlantis;
import atlantis.debug.APainter;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Bullet;
import bwapi.BulletType;
import bwapi.Color;

import java.util.List;


public class AAvoidSpells {

    public static boolean avoidSpellsAndMines(AUnit unit) {

        // === Psionic Storm ========================================
        
        if (unit.isUnderStorm()) {
            for (Bullet bullet : Atlantis.game().getBullets()) {

                // PSIONIC STORM
                if (bullet.getType().equals(BulletType.Psionic_Storm)) {
//                    System.err.println("------------- " + A.now() + " PSIONIC! ----------------");

                    if (handleMoveAwayIfCloserThan(unit, APosition.create(bullet.getPosition()), 3.2)) {
                        unit.setTooltip("Psionic Storm!");
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

        int radius = Math.max(6, canShootAtMines ? unit.getGroundWeapon().maxRange() + 3 : 0);
        List<AUnit> mines = Select.allOfType(AUnitType.Terran_Vulture_Spider_Mine).inRadius(radius, unit).listUnits();
        for (AUnit mine : mines) {

            // Our mine
            if (mine.isOur()) {
                if (mine.isMoving() && mine.distTo(unit) <= 3.5) {
                    unit.moveAwayFrom(mine.position(), 2, "Avoid mine!");
                    return true;
                }
            }

            // Enemy mine
            else {
                if (canShootAtMines) {
                    if (handleEnemyMineAsRangedUnit(unit, mine)) {
                        unit.setTooltip("ShootMine");
                        return true;
                    }
                }
                else {
                    if (handleEnemyMineAsMeleeUnit(unit, mine)) {
                        unit.setTooltip("AvoidMine");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean handleEnemyMineAsMeleeUnit(AUnit unit, AUnit mine) {
        unit.moveAwayFrom(mine.position(), 1, "Avoid mine!");
        APainter.paintLine(unit, mine, Color.Yellow);
        return true;
    }

    private static boolean handleEnemyMineAsRangedUnit(AUnit unit, AUnit mine) {
        if (mine.distTo(unit) <= 2.0) {
            unit.runningManager().runFrom(mine, 3);
            unit.setTooltip("AVOID MINE(" + mine.distTo(unit) + ")");
            return true;
        }

        unit.attackUnit(mine);
        unit.setTooltip("SHOOT MINE");
        return true;
    }

    private static boolean handleMoveAwayIfCloserThan(AUnit unit, APosition avoidCenter, double minDist) {
        if (unit.distTo(avoidCenter) < minDist) {
            unit.moveAwayFrom(avoidCenter, 3, "AvoidSpell");
            return true;
        }
        else {
            return false;
        }
    }
    
}
