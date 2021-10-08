package atlantis.combat.micro;

import atlantis.Atlantis;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.Bullet;
import bwapi.BulletType;
import bwapi.Position;
import java.util.List;


public class ABadWeather {

    public static boolean avoidSpellsMines(AUnit unit) {
        boolean canShootAtMines = unit.isRangedUnit() && unit.canAttackGroundUnits();
        
        // === Psionic Storm ========================================
        
        if (unit.isUnderStorm()) {
            for (Bullet bullet : Atlantis.game().getBullets()) {

                // PSIONIC STORM
                if (bullet.getType().equals(BulletType.Psionic_Storm)) {
                    if (handleMoveAwayIfCloserThan(unit, bullet.getPosition(), 3.2)) {
                        unit.setTooltip("Psionic Storm!");
                        return true;
                    }
                }
            }
        }
        
        // === Mines ===============================================

        int radius = Math.max(7, canShootAtMines ? unit.getGroundWeapon().maxRange() + 3 : 0);
        List<AUnit> mines = Select.allOfType(AUnitType.Terran_Vulture_Spider_Mine).inRadius(radius, unit).listUnits();
        for (AUnit mine : mines) {
            
            // Our mine
            if (mine.isOurUnit()) {
                if (mine.isMoving() && mine.distanceTo(unit) < 3.5) {
                    unit.moveAwayFrom(mine.getPosition(), 1, "Avoid mine!");
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
//                if (mine.isVisible() && unit.getGroundWeaponCooldown() > 0) {
//                    unit.moveAwayFrom(mine.getPosition(), 1);
//                    unit.setTooltip("Avoid mine!");
//                    return true;
//                }
            }
        }
        
        // =========================================================        
        
        return false;
    }

    // =========================================================

    private static boolean handleEnemyMineAsMeleeUnit(AUnit unit, AUnit mine) {
        unit.moveAwayFrom(mine.getPosition(), 1, "Avoid mine!");
        return true;
    }

    private static boolean handleEnemyMineAsRangedUnit(AUnit unit, AUnit mine) {
        unit.attackUnit(mine);
        unit.setTooltip("SHOOT MINE");
        return true;
    }

    private static boolean handleMoveAwayIfCloserThan(AUnit unit, Position avoidCenter, double minDist) {
        if (unit.distanceTo(avoidCenter) < 3.2) {
            unit.moveAwayFrom(avoidCenter, 2, "Avoid effect");
            return true;
        }
        else {
            return false;
        }
    }
    
}
