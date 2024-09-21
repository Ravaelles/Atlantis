package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.DoAvoidEnemies;
import atlantis.combat.micro.avoid.WantsToAvoid;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class ScoutSeparateFromCloseEnemies extends Manager {
    private AUnit enemy;

    public ScoutSeparateFromCloseEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemy = unit.enemiesNear().combatUnits().canAttack(unit, safetyMargin()).nearestTo(unit);

        return enemy != null;
    }


    private double safetyMargin() {
        return 3.1
            + (Enemy.zerg() ? 1.5 : 0)
            + (unit.lastUnderAttackLessThanAgo(30 * 5) ? 2.5 : 0)
            + unit.woundPercent() / 25.0;
    }

    @Override
    public Manager handle() {
        Manager manager = (new DoAvoidEnemies(unit)).handle();
        if (manager != null) return usedManager(this);

        return null;
    }
}
