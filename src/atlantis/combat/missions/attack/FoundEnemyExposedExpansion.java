package atlantis.combat.missions.attack;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class FoundEnemyExposedExpansion {
    private static Cache<AFocusPoint> cache = new Cache<>();

    public static AFocusPoint getItFound() {
        return cache.getIfValid(
            "enemyExpansion",
            247,
            () -> {
                APosition enemyMain = EnemyInfo.enemyMain();
                if (enemyMain == null) return null;

                for (AUnit base : EnemyUnits.discovered().bases().list()) {
                    if (
                        base != null
                            && base.hasPosition()
                            && base.isAlive()
                            && base.groundDist(enemyMain) >= 33
                    ) {
                        return new AFocusPoint(
                            base,
                            Select.mainOrAnyBuilding(),
                            "AttackEnemyExpansion(" + base.distTo(Select.mainOrAnyBuilding()) + ")"
                        );
                    }
                }

                return null;
            }
        );
    }
}
