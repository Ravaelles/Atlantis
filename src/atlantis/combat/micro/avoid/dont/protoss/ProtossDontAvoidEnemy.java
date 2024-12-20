package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class ProtossDontAvoidEnemy extends HasUnit {
    public ProtossDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    public boolean applies() {
        if (!unit.isProtoss()) return false;

        if (unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)) return true;

        if (dontAvoidWhenCannonsNear(unit)) return true;

        if (ZealotDontAvoidEnemy.dontAvoid(unit)) return true;
        if (DTDontAvoidEnemy.dontAvoid(unit)) return true;
        if (DragoonDontAvoidEnemy.dontAvoid(unit)) {
//            System.out.println("DragoonDontAvoidEnemy " + unit.tooltip());
            return true;
        }

//        if (unit.isShuttle() && unit.isAction(Actions.LOAD)) {
        if (unit.isShuttle()) {
//            unit.addLog("ShuttleLoadNoAvoid");
            return true;
        }

        return false;
    }

    private boolean dontAvoidWhenCannonsNear(AUnit unit) {
//        if (DontAvoidWhenCannonsNear.check(unit) && (unit.isMelee() || unit.cooldown() <= 7)) {
        if (unit.cooldown() <= 9 && DontAvoidWhenCannonsNear.check(unit)) {
            unit.addLog("SupportCannon");
            return true;
        }

        return false;
    }
}
