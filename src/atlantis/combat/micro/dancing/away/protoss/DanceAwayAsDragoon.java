package atlantis.combat.micro.dancing.away.protoss;

import atlantis.decions.Decision;
import atlantis.protoss.ProtossFlags;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class DanceAwayAsDragoon extends HasUnit {
    private final AUnit enemy;

    public DanceAwayAsDragoon(AUnit unit, AUnit enemy) {
        super(unit);
        this.enemy = enemy;
    }

    public Decision applies() {
        Decision decision;

        if (tooHealthy()) return Decision.FALSE;
        if (unit.lastAttackFrameMoreThanAgo(30 * 3)) return Decision.FALSE;
        if (provideSupportForMelee()) return Decision.FALSE;


        if (unit.enemiesNear().inRadius(8, unit).notEmpty()) {
            if (dragoonLowHpAndStillUnderAttack()) return Decision.TRUE;
            if ((decision = vsEnemyDragoons()).notIndifferent()) return decision;
        }

        if (quiteHealthyAndNotUnderAttack()) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }

    private boolean provideSupportForMelee() {
        return unit.hp() > 20
            && unit.friendsNear().combatUnits().melee().inRadius(7, unit).notEmpty();
    }

    private boolean quiteHealthyAndNotUnderAttack() {
        return unit.hp() >= 40
            && unit.lastUnderAttackMoreThanAgo(30 * 4);
    }

    private boolean tooHealthy() {
        if (unit.enemiesNear().inRadius(7, unit).onlyMelee()) return unit.shieldDamageAtMost(19);

        return unit.shields() >= 40;
    }

    private Decision vsEnemyDragoons() {
        if (unit.enemiesNear().dragoons().canAttack(unit, 0.1).notEmpty()) return Decision.INDIFFERENT;

        if (unit.shields() >= 40) return Decision.FALSE;

        if (unit.enemiesNearInRadius(enemiesRadius()) > 0) return Decision.TRUE;

        return Decision.FALSE;
    }

    private double enemiesRadius() {
        return 4.1
            + (enemy.isFacing(unit) ? 0.4 : -1.6)
            + (unit.hp() <= 60 ? 0.7 : 0);
    }

    private boolean dragoonLowHpAndStillUnderAttack() {
        return unit.isDragoon()
            && !ProtossFlags.dragoonBeBrave()
            && unit.hp() <= 60
            && (
            unit.lastUnderAttackLessThanAgo(90)
                || unit.enemiesNearInRadius(enemiesRadius()) > 0
        );
    }
}
