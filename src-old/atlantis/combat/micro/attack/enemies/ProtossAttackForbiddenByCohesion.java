package atlantis.combat.micro.attack.enemies;

import atlantis.units.AUnit;

public class ProtossAttackForbiddenByCohesion {
    public static boolean forbiddenToAttack(AUnit unit) {
//        if (unit.isLeader()) return false;
        if (unit.isMelee()) return false;
        if (unit.enemiesICanAttack(0).notEmpty()) return false;

        if (!unit.squad().isCohesionPercentOkay()) return true;

        return false;
    }
}
