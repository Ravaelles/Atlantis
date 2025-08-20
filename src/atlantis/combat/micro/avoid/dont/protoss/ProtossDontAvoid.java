package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.combat.micro.avoid.dont.protoss.dragoon.DragoonDontAvoid;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossDontAvoid extends HasUnit {
    public ProtossDontAvoid(AUnit unit) {
        super(unit);
    }

    public boolean dontAvoid() {
        if (!We.protoss()) return false;

//        if (true) return false;

        if (unit.effUndetected() && noEnemyDetectorsNearby()) {
            unit.addLog("CloakedNoAvoid");
            return true;
        }

        if (DTDontAvoid.dontAvoid(unit)) return true;
        if (DragoonDontAvoid.dontAvoid(unit)) return true;
        if (unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)) return true;
        if (dontAvoidWhenCannonsNear(unit)) return true;
        if (ZealotDontAvoidEnemy.dontAvoid(unit)) return true;
        if (unit.isShuttle() && unit.hp() >= 42 && unit.isAction(Actions.LOAD)) {
//        if (unit.isShuttle()) {
//            unit.addLog("ShuttleLoadNoAvoid");
            return true;
        }

        return false;
    }

    private boolean noEnemyDetectorsNearby() {
        return unit.enemiesNear().detectors().countInRadius(8.5, unit) == 0;
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
