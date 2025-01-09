package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.Strategy;
import atlantis.units.AUnit;

public class TerranCheeseDontAvoidEnemy extends Manager {
    public TerranCheeseDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Strategy.get().isRushOrCheese() && A.s <= 60 * 7 && EnemyUnits.ranged() <= maxEnemyRangedUnits();
    }

    private int maxEnemyRangedUnits() {
        int our = 1 + unit.friendsNear().count();
        int enemies = unit.enemiesNear().ranged().combatUnits().count();

        if (Enemy.protoss()) return 1 + (our / 5);
        if (Enemy.terran()) {
            return 1 + our - enemies;
        }

        return 1 + (our / 3);
    }
}
