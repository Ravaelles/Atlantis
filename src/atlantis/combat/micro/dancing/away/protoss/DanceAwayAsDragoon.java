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
        if (tooHealthy()) return Decision.FALSE;
        if (provideSupportForMelee()) return Decision.FALSE;

        if (unit.enemiesNear().inRadius(8, unit).notEmpty()) {
            if (dragoonLowHpAndStillUnderAttack()) return Decision.TRUE;
            if (dragoonEnemyClose()) return Decision.TRUE;
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
        return unit.shieldDamageAtMost(9);
    }

    private boolean dragoonEnemyClose() {
        return unit.isDragoon()
//            && unit.lastAttackFrameLessThanAgo(150)
            && unit.enemiesNearInRadius(enemiesRadius()) > 0;
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
