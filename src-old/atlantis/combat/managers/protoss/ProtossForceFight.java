package atlantis.combat.managers.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;

public class ProtossForceFight extends Manager {
    public ProtossForceFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (A.s <= 30 * 8) return false;
        if (unit.cooldown() >= 8) return false;
//        if (unit.eval() <= 0.3) return false;
        if (unit.isAttacking()) return false;

        AUnit leader = unit.leader();
        if (leader != null && leader.isAttackingRecently()) return true;

        if (unit.ourBuildingsNearCount(3) > 0) return true;

        AChoke choke = unit.nearestChoke();
        if (choke == null) return false;
        if (!choke.isNaturalChoke() && !choke.isMainChoke()) return false;

        return unit.distTo(choke.center()) <= 5;
    }

    @Override
    public Manager handle() {
        AttackNearbyEnemies attackManager = new AttackNearbyEnemies(unit);

        if (attackManager.forceHandled()) {
            return usedManager(this);
        }

        return null;
    }
}
