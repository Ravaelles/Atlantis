package atlantis.information.strategy;

import atlantis.game.player.Enemy;

public class UnknownStrategy extends AStrategy {

    public UnknownStrategy() {
        setName("Unknown");
        unknown = true;

        if (Enemy.zerg()) {
            setGoingRush();
        }
    }

}
