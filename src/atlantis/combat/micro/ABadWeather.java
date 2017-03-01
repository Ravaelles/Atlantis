package atlantis.combat.micro;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.Bullet;
import bwapi.BulletType;
import bwapi.Position;
import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ABadWeather {

    public static boolean avoidPsionicStormAndActiveMines(AUnit unit) {
        boolean isRangedUnit = unit.isRangedUnit();
        
        // === Psionic Storm ========================================
        
        if (unit.isUnderStorm()) {
            for (Bullet bullet : Atlantis.getBwapi().getBullets()) {

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
        
        List<AUnit> mines = Select.allOfType(AUnitType.Terran_Vulture_Spider_Mine).inRadius(4, unit).listUnits();
        for (AUnit mine : mines) {
            if (!mine.isCloaked() && mine.distanceTo(unit) < 3) {
                if (mine.isOurUnit() && mine.isMoving()) {
                    unit.moveAwayFrom(mine.getPosition(), 1);
                    unit.setTooltip("Avoid mine!");
                    return true;
                }
                else if (unit.getGroundWeaponCooldown() > 0) {
                    unit.moveAwayFrom(mine.getPosition(), 1);
                    unit.setTooltip("Avoid mine!");
                    return true;
                }
            }
        }
        
        // =========================================================        
        
        return false;
    }
    
    // =========================================================

    
    private static boolean handleMoveAwayIfCloserThan(AUnit unit, Position avoidCenter, double minDist) {
        if (unit.distanceTo(avoidCenter) < 3.2) {
            unit.moveAwayFrom(avoidCenter, 2);
            return true;
        }
        else {
            return false;
        }
    }
    
}
