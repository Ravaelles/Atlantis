package atlantis.combat.micro.avoid.always;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import atlantis.game.player.Enemy;

import atlantis.architecture.Manager;

public class DragoonAlwaysAvoidEnemy extends Manager {
    public DragoonAlwaysAvoidEnemy(AUnit unit) {
        super(unit);
    }

    private boolean t(String reason) {
//        unit.setTooltip(reason);
        return true;
    }

    @Override
    public boolean applies() {
        if (!unit.isDragoon()) return false;

//        if (seriouslyWoundedAndEnemiesNear()) return true;

        if (Enemy.zerg()) return vsZerg();
        if (Enemy.protoss()) return vsProtoss();

        return false;
    }

    private boolean vsProtoss() {
        if (A.s <= 5 * 60 && unit.shotSecondsAgo(4) && unit.meleeEnemiesNearCount(3.3) >= 2) {
            return t("GvZeal_vP");
        }

        if (
            unit.hp() <= 62
//                && unit.isMissionAttack()
                && (unit.hp() <= 25 || unit.shotSecondsAgo(5))
                && (
                    unit.enemiesNear().melee().inRadius(3.7, unit).notEmpty()
                        || unit.enemiesNear().ranged().inRadius(OurDragoonRange.range() - 0.1, unit).notEmpty()
            )
        ) {
            return t("VeryWounded_vP");
        }

        return false;
    }

    private boolean vsZerg() {
        if (unit.meleeEnemiesNearCount(2.7) >= 3) {
            return t("AvoidTheSwarm");
        }

        if (unit.woundHp() <= 3 && unit.enemiesThatCanAttackMe(0.8).empty()) {
            return false;
        }

        if (isAnyEnemyThatCanAttackUsRelativelyClose()) {
            return t("CloseZergy");
        }

        if (unit.hp() <= 50 && unit.enemiesThatCanAttackMe(1.5).atLeast(3)) {
            return t("WantsToLive");
        }

        if (
            Enemy.zerg()
                && unit.hp() <= 50
                && unit.isMissionAttack()
                && unit.shotSecondsAgo(2.5)
                && (
                    unit.enemiesNear().ranged().inRadius(OurDragoonRange.range() - 0.4, unit).notEmpty()
                    || unit.enemiesNear().melee().inRadius(3, unit).notEmpty()
            )
        ) {
            return t("VeryWounded_vZ");
        }

        if (
            unit.eval() <= 1.1
                && unit.shieldWound() >= 36
//                && unit.shotSecondsAgo(1.5)
                && unit.cooldown() >= 4
                && unit.meleeEnemiesNearCount(OurDragoonRange.range() - 0.8) >= 2
        ) {
            return t("CarefulGoon");
        }

        if (
            unit.hp() <= 60
                && unit.shotSecondsAgo() <= 3.5
                && unit.enemiesThatCanAttackMe(1.85).ranged().atLeast(2)
        ) {
            return t("Goon2Enemies");
        }

//        if (
//            unit.cooldown() >= 11
//                && unit.shotSecondsAgo(1)
//                && unit.friendsNear().atMost(20)
//                && unit.enemiesNearInRadius(OurDragoonRange.range() - 0.6) >= 1
//        ) {
//            return unit.lastAttackFrameLessThanAgo(30 * (unit.hp() >= 60 ? 4 : 7))
//                && reason("GoonUA");
//        }

        if (
            unit.cooldown() >= 15
                && unit.shieldDamageAtLeast(41)
                && unit.lastUnderAttackLessThanAgo(30 * 2)
                && unit.lastAttackFrameLessThanAgo(50)
                && unit.enemiesNearInRadius(OurDragoonRange.range() - 0.5) >= 2
        ) return t("GoonCooldown");

        if (unit.shieldDamageAtLeast(9)) {
            if (lonelyAndLotsOfZerglings()) return t("GoonLingz");
            if (lonelyAndLotsOfHydras()) return t("GoonHydraz");
        }

        return false;
    }

    private boolean isAnyEnemyThatCanAttackUsRelativelyClose() {
//        System.out.println(unit.shotSecondsAgo() + " / " + unit.cooldown());
//        System.out.println(unit.cooldown() + " / " + unit.lastAttackFrameAgo());
        return unit.shieldWounded()
            && !unit.isAttacking()
            && unit.cooldown() >= 11
//            && unit.shotSecondsAgo(1)
            && !unit.isAttackingBuilding()
            && unit.enemiesNear().melee().canAttack(unit, 2.8).notEmpty();
    }

    private boolean lonelyAndLotsOfHydras() {
        return unit.friendsNear().inRadius(2.5, unit).atMost(0)
            && unit.shotSecondsAgo(2)
            && unit.enemiesNear().hydras().inRadius(7.2, unit).atLeast(unit.almostDead() ? 1 : 3);
    }

    private boolean lonelyAndLotsOfZerglings() {
        return unit.friendsNear().inRadius(2.5, unit).atMost(1)
            && unit.enemiesNear().zerglings().inRadius(3.2, unit).atLeast(unit.almostDead() ? 2 : 3);
    }

    private boolean seriouslyWoundedAndEnemiesNear() {
        if (true) return false;

        if (unit.isRunningOrRetreating()) return false;
        if (unit.hp() > A.whenEnemyProtossZerg(40, 65)) return false;
        if (unit.hp() >= 23 && unit.eval() >= 1.5) return false;

        boolean heavilyWounded = unit.hp() <= 40;
        return (
                unit.meleeEnemiesNearCount(OurDragoonRange.range() - 0.3) > 0
                    || unit.rangedEnemiesCount(rangedEnemiesSafetyMargin(heavilyWounded)) >= (heavilyWounded ? 1 : 2)
            ) && t("SeriouslyWounded&EnemiesNear");
    }

    private double rangedEnemiesSafetyMargin(boolean heavilyWounded) {
        return (heavilyWounded ? 1 : 0.5)
            + (unit.cooldown() >= 15 ? 1 : 0);
    }
}
