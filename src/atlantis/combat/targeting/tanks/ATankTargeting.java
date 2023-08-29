package atlantis.combat.targeting.tanks;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;

import java.util.ArrayList;
import java.util.List;

public class ATankTargeting extends HasUnit {
    public ATankTargeting(AUnit unit) {
        super(unit);
    }

    public AUnit defineTarget() {
        List<AUnit> enemies = unit.enemiesNear()
            .visibleOnMap()
            .havingAtLeastHp(1)
            .havingPosition()
            .realUnitsAndBuildings()
            .groundUnits()
            .effVisible()
            .sortDataByDistanceTo(unit, true);

        ArrayList<AUnit> possibleTargets = new ArrayList<>();
        for (AUnit enemy : enemies) {
            if (enemyCanBePhysicallyAttacked(enemy)) {
                possibleTargets.add(enemy);
            }
        }

        return (new HighestScoreTargetForTank(unit)).targetWithBestScoreAmong(possibleTargets);
    }

    private boolean enemyCanBePhysicallyAttacked(AUnit enemy) {
        double distToEnemy = unit.distTo(enemy);
        return 2.05 <= distToEnemy && distToEnemy <= 11.95;
    }
}
