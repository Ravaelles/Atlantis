package atlantis.combat.micro.dancing.away.protoss;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.protoss.ProtossFlags;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class DanceAwayAsDragoon extends HasUnit {
    private final AUnit enemy;

    public DanceAwayAsDragoon(AUnit unit, AUnit enemy) {
        super(unit);
        this.enemy = enemy;
    }

    public Decision applies() {
        if (A.supplyUsed() >= 160) return Decision.FALSE;

        Decision decision;

//        if (unit.lastActionLessThanAgo(2, Actions.ATTACK_UNIT)) return Decision.FALSE;

        if (unit.lastAttackFrameMoreThanAgo(30 * 8)) return Decision.FALSE;
        if (unit.cooldown() <= (unit.hp() <= 40 ? 3 : 12)) return Decision.FALSE;

        if (Enemy.protoss()) {
            if ((decision = vsEnemyDragoons()).notIndifferent()) return decision;
        }

        else if (Enemy.zerg()) {
            if ((decision = vsEnemyHydra()).notIndifferent()) return decision;
//            if ((decision = vsEnemyZergling()).notIndifferent()) return decision;
        }

        if (!Enemy.zerg() && EnemyInfo.hasRanged()) {
            if (true) return Decision.FALSE;
        }

//        if (unit.woundHp() <= 14 && unit.lastAttackFrameMoreThanAgo(30 * 5)) return Decision.FALSE;
        if ((unit.cooldown() >= 12 || unit.hp() <= 100) && !unit.isSafeFromMelee()) return Decision.TRUE;

        if (tooHealthy()) return Decision.FALSE;
        if (provideSupportForMelee()) return Decision.FALSE;

        if (unit.enemiesNear().inRadius(8, unit).notEmpty()) {
            if (dragoonLowHpAndStillUnderAttack()) return Decision.TRUE;
        }

        if (quiteHealthyAndNotUnderAttack()) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }

    private Decision vsEnemyHydra() {
        Selection hydras = unit.enemiesNear().hydras();
        if (hydras.empty()) return Decision.INDIFFERENT;

        double range = 3.85 + (ResearchSingularityCharge.isResearched() ? 2 : 0);

        return hydras.countInRadius(range, unit) > 0
            ? Decision.TRUE
            : Decision.INDIFFERENT;
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
        if (unit.hp() >= 62 && unit.lastAttackFrameMoreThanAgo(100)) return Decision.FALSE;

        if (unit.enemiesNear().dragoons().canAttack(unit, 0).empty()) return Decision.INDIFFERENT;

        if (unit.hp() <= 82 && unit.cooldown() >= 10) return Decision.TRUE;

        return (unit.meleeEnemiesNearCount(meleeEnemiesRadius()) > 0)
            ? Decision.TRUE : Decision.FALSE;

//        if (unit.shields() >= 40) return Decision.FALSE;
//
//        if (unit.enemiesNearInRadius(enemiesRadius()) > 0) return Decision.TRUE;
//
//        return Decision.FALSE;
    }

    private double meleeEnemiesRadius() {
        return 1.4
            + (enemy.isFacing(unit) ? 0.4 : -1.6)
            + (unit.hp() <= 60 ? 0.7 : 0);
    }

    private boolean dragoonLowHpAndStillUnderAttack() {
        return unit.isDragoon()
            && !ProtossFlags.dragoonBeBrave()
            && unit.hp() <= 60
            && (
            unit.lastUnderAttackLessThanAgo(90)
                || unit.enemiesNearInRadius(meleeEnemiesRadius()) > 0
        );
    }
}
