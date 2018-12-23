package atlantis.combat.micro;

import atlantis.Atlantis;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import org.openbw.bwapi4j.Bullet;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.BulletType;
import org.openbw.bwapi4j.type.Color;

import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ABadWeather {

    public static boolean avoidPsionicStormAndActiveMines(AUnit unit) {

        // === Psionic Storm ========================================
        
        if (unit.isUnderStorm()) {
            for (Bullet bullet : Atlantis.getBW().getBullets()) {

                // PSIONIC STORM
                if (bullet.getType().equals(BulletType.Psionic_Storm)) {
                    if (handleMoveAwayIfCloserThan(unit, bullet.getPosition())) {
                        unit.setTooltip("Psionic Storm!");
                        APainter.paintLine(unit, bullet.getPosition(), Color.WHITE);
                        return true;
                    }
                }
            }
        }
        
        // === Mines ===============================================
        
        List<AUnit> mines = Select.allOfType(AUnitType.Terran_Vulture_Spider_Mine).inRadius(4, unit).listUnits();
        for (AUnit mine : mines) {
            
            // Our mine
            if (mine.isOurUnit()) {
                if (mine.isMoving() && mine.distanceTo(unit) < 3.5) {
                    unit.moveAwayFrom(mine.getPosition(), 1);
                    unit.setTooltip("Avoid mine!");
                    return true;
                }
            }
            
            // Enemy mine
            else {
                if (mine.isVisible() && unit.getGroundWeaponCooldown() > 0) {
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

    
    private static boolean handleMoveAwayIfCloserThan(AUnit unit, Position avoidCenter) {
        if (unit.distanceTo(avoidCenter) < 3.2) {
            unit.moveAwayFrom(avoidCenter, 2);
            return true;
        }
        else {
            return false;
        }
    }
    
}
