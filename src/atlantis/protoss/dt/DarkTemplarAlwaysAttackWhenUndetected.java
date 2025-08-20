package atlantis.protoss.dt;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;

public class DarkTemplarAlwaysAttackWhenUndetected extends Manager {
    public DarkTemplarAlwaysAttackWhenUndetected(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.effVisible()) {
            if (unit.woundHp() >= 13) return false;
            if (unit.shieldWounded() && unit.eval() <= 0.6) return false;
        }

        if (preventChasingEnemiesTooLong()) return false;

        return true;
    }

    @Override
    public Manager handle() {
        if (attackBestEnemies()) return usedManager(this);
        if (regularAttack()) return usedManager(this);

        return null;
    }

    private boolean preventChasingEnemiesTooLong() {
        return unit.isAttacking()
            && (A.s % 6 <= 3)
            && unit.hasValidTarget()
            && unit.target().isMoving()
            && unit.target().speedIsQuickerOrEqual(unit)
            && unit.isOtherUnitShowingBackToUs(unit.target());
    }

    private boolean attackBestEnemies() {
        if (unit.isMoving() && unit.cooldown() >= 10) return false;

        AUnit target = DarkTemplarTargeting.targetFor(unit);
        if (target != null) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(target)) {
                return true;
            }
        }

        return false;
    }

    private boolean regularAttack() {
        AttackNearbyEnemies attackNearbyEnemies = new AttackNearbyEnemies(unit);
        if (attackNearbyEnemies.forceHandled()) {
            return true;
        }
        return false;
    }
}
