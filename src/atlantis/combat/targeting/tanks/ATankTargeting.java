package atlantis.combat.targeting.tanks;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;

import java.util.ArrayList;
import java.util.List;

public class ATankTargeting extends HasUnit {
    public ATankTargeting(AUnit unit) {
        super(unit);
    }

    public AUnit targetForTank() {
        List<AUnit> enemies = targets();

        ArrayList<AUnit> possibleTargets = new ArrayList<>();
        for (AUnit enemy : enemies) {
            if (enemyCanBePhysicallyAttacked(enemy)) {
                possibleTargets.add(enemy);
            }
        }

        AUnit target = null;

        if ((target = (new TankCrucialTargeting(unit, enemies)).crucialTarget()) != null) return target;

        return (new HighestScoreTargetForTank(unit)).targetWithBestScoreAmong(possibleTargets);
    }

    private List<AUnit> targets() {
        return unit.enemiesNear()
            .groundUnits()
            .visibleOnMap()
            .havingAtLeastHp(1)
            .havingPosition()
            .realUnitsAndBuildings()
            .effVisible()
            .sortDataByDistanceTo(unit, true);
    }

    private boolean enemyCanBePhysicallyAttacked(AUnit enemy) {
        double distToEnemy = unit.distTo(enemy);

        if (unit.isSieged()) return 2.05 <= distToEnemy && distToEnemy <= 11.99;

        return distToEnemy <= 7;
    }
}
