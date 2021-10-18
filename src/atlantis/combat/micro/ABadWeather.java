package atlantis.combat.micro;

import atlantis.Atlantis;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.Bullet;
import bwapi.BulletType;
import bwapi.Position;
import java.util.List;


public class ABadWeather {

    public static boolean avoidSpellsAndMines(AUnit unit) {

        // === Psionic Storm ========================================
        
        if (unit.isUnderStorm()) {
            for (Bullet bullet : Atlantis.game().getBullets()) {

                // PSIONIC STORM
                if (bullet.getType().equals(BulletType.Psionic_Storm)) {
                    if (handleMoveAwayIfCloserThan(unit, APosition.create(bullet.getPosition()), 3.2)) {
                        unit.setTooltip("Psionic Storm!");
                        return true;
                    }
                }
            }
        }
        
        // === Mines ===============================================

        return handleMines(unit);
        
        // =========================================================
    }

    // =========================================================

    private static boolean handleMines(AUnit unit) {
        boolean canShootAtMines = unit.isRangedUnit() && unit.canAttackGroundUnits();

        int radius = Math.max(7, canShootAtMines ? unit.getGroundWeapon().maxRange() + 3 : 0);
        List<AUnit> mines = Select.allOfType(AUnitType.Terran_Vulture_Spider_Mine).inRadius(radius, unit).listUnits();
        for (AUnit mine : mines) {

            // Our mine
            if (mine.isOurUnit()) {
                if (mine.isMoving() && mine.distanceTo(unit) <= 3.5) {
                    unit.moveAwayFrom(mine.getPosition(), 2, "Avoid mine!");
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
        unit.moveAwayFrom(mine.getPosition(), 1, "Avoid mine!");
        return true;
    }

    private static boolean handleEnemyMineAsRangedUnit(AUnit unit, AUnit mine) {
        if (mine.distanceTo(unit) <= 2.0) {
            unit.runFrom(mine, 3);
            unit.setTooltip("AVOID MINE(" + mine.distanceTo(unit) + ")");
            return true;
        }

        unit.attackUnit(mine);
        unit.setTooltip("SHOOT MINE");
        return true;
    }

    private static boolean handleMoveAwayIfCloserThan(AUnit unit, APosition avoidCenter, double minDist) {
        if (unit.distanceTo(avoidCenter) < minDist) {
            unit.moveAwayFrom(avoidCenter, 3, "Avoid effect");
            return true;
        }
        else {
            return false;
        }
    }
    
}
