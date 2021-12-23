package atlantis.strategy;

import atlantis.util.Enemy;

public class UnknownStrategy extends AStrategy {

    public UnknownStrategy() {
        setName("Unknown");
        unknown = true;

        if (Enemy.zerg()) {
            setGoingRush();
        }
    }

}
