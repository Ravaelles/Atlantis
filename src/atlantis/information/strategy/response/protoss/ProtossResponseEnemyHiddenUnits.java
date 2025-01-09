package atlantis.information.strategy.response.protoss;

import atlantis.game.player.Enemy;
import atlantis.information.strategy.response.StrategyResponse;
import atlantis.production.dynamic.protoss.buildings.ProduceCannonAtNatural;

public class ProtossResponseEnemyHiddenUnits extends StrategyResponse {
    public void handle() {
        int howMany = Enemy.zerg()
            ? 2
            : (Enemy.protoss() ? 3 : 2);

        for (int i = 0; i < howMany; i++) {
            ProduceCannonAtNatural.produce();
        }
    }
}
