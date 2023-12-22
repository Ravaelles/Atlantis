package atlantis.combat.micro.avoid.fight;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ShouldFightInsteadAvoidAsRanged extends Manager {
    public ShouldFightInsteadAvoidAsRanged(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isRanged()
            && unit.enemiesNear().melee().inRadius(3, unit).empty();
    }

    @Override
    protected Manager handle() {
//        System.out.println("@ " + A.now() + " -- "
//            + unit.manager() + " / " +
//            "dist: " + unit.nearestEnemyDist() + " / " +
//            "cooldown: " + unit.cooldown());

        if ((new AttackNearbyEnemies(unit)).invoke(this) != null) {
            return usedManager(this);
        }

        return null;
    }
}
