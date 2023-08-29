package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeAgainstTerran extends Manager {
    public SiegeAgainstTerran(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!Enemy.terran()) return false;

        return areEnemiesInRange();
    }

    private boolean areEnemiesInRange() {
        Selection enemies = EnemyUnits.discovered().groundUnits().combatUnits();

        if (enemies.atLeast(2)) return true;

        return unit.id() % 3 == 0 || enemies.inRadius(15.9, unit).excludeMarines().notEmpty();
    }

    @Override
    protected Manager handle() {
        if (WantsToSiege.wantsToSiegeNow(unit, "Enemies!")) return usedManager(this);

        return null;
    }
}
