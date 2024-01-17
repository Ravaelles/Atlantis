package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

import java.util.List;

public class SpreadFire {
    private final AUnit ourUnit;
    private final Selection targets;

    public SpreadFire(AUnit ourUnit, Selection targets) {
        this.ourUnit = ourUnit;
        this.targets = targets;
    }

    protected boolean shouldSpreadFire(AUnit ourUnit, Selection targets) {
        return Enemy.zerg() && targets.notEmpty() && ourUnit.isRanged();
    }

    protected AUnit spreadFire(AUnit ourUnit, Selection targets) {
        List<AUnit> enemies = targets.sortByHealth().limit(3).list();

        // Randomize enemy target based on unit id
        AUnit randomPeasant = enemies.get(ourUnit.id() % enemies.size());
        if (randomPeasant != null) {
            return randomPeasant;
        }

        return targets.mostWounded();
    }
}
