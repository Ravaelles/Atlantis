package atlantis.combat.missions;

import atlantis.AViewport;
import atlantis.debug.APainter;
import atlantis.enemy.AEnemyUnits;
import atlantis.map.ABaseLocation;
import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Color;

public class MissionContainFocusPointManager extends MissionFocusPointManager {

    private APosition containEnemyAtPoint = null;

    @Override
    public APosition focusPoint() {
        if (containEnemyAtPoint != null) {
                APainter.paintCircle(containEnemyAtPoint, 20, Color.Teal);
                APainter.paintCircle(containEnemyAtPoint, 18, Color.Teal);
            APainter.paintCircle(containEnemyAtPoint, 16, Color.Teal);
            return containEnemyAtPoint;
        }

        APosition enemyBase = AEnemyUnits.getEnemyBase();
        if (enemyBase == null) {
            return containPointIfEnemyBaseNotKnown();
        }

        return containEnemyAtPoint = containPointIfEnemyBaseIsKnown(enemyBase);
    }

    // =========================================================

    private APosition containPointIfEnemyBaseIsKnown(APosition enemyBase) {
        AChokepoint chokepoint = AMap.getChokepointForNaturalBase(enemyBase.getPosition());
        if (chokepoint != null) {
            AViewport.centerScreenOn(chokepoint.getCenter());
            return containEnemyAtPoint = chokepoint.getCenter();
        }

        APosition natural = AMap.getNaturalBaseLocation(enemyBase.getPosition()).getPosition();
        if (natural == null) {
            AViewport.centerScreenOn(natural);
        }

        return containEnemyAtPoint = natural;
    }

    private APosition containPointIfEnemyBaseNotKnown() {
        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            return null;
        }

        AChokepoint choke = AMap.getChokepointForNaturalBase(mainBase.getPosition());
        return choke == null ? null : choke.getCenter();
    }

}
