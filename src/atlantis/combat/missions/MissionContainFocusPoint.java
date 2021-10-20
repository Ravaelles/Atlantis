package atlantis.combat.missions;

import atlantis.AViewport;
import atlantis.debug.APainter;
import atlantis.enemy.AEnemyUnits;
import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Color;

public class MissionContainFocusPoint extends MissionFocusPoint {

    private APosition containEnemyAtPoint = null;

    @Override
    public APosition focusPoint() {
        if (containEnemyAtPoint != null) {
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
            AViewport.centerCameraOn(chokepoint.getCenter());
            return containEnemyAtPoint = chokepoint.getCenter();
        }

        APosition natural = AMap.getNaturalBaseLocation(enemyBase.getPosition()).getPosition();
        if (natural == null) {
            AViewport.centerCameraOn(natural);
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
