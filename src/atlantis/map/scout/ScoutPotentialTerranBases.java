package atlantis.map.scout;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;

public class ScoutPotentialTerranBases extends ScoutPotentialEnemyBases {
    public ScoutPotentialTerranBases(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Enemy.terran() && super.applies();
    }
}
