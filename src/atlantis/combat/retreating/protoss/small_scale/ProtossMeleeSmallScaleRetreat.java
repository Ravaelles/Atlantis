package atlantis.combat.retreating.protoss.small_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.combat.retreating.protoss.big_battle.ProtossTooBigBattleToRetreat;
import atlantis.combat.retreating.protoss.big_battle.ProtossTooBigBattleToRetreat_asZealot;
import atlantis.combat.retreating.protoss.should.ProtossRetreatWrapper;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class ProtossMeleeSmallScaleRetreat extends Manager {
    private Selection friends;
    private Selection enemies;

    public ProtossMeleeSmallScaleRetreat(AUnit unit) {
        super(unit);

        friends = ProtossRetreatWrapper.friends(unit);
        enemies = ProtossRetreatWrapper.enemies(unit);
    }

    private boolean f(String reasonWhyNot) {
//        System.out.println("SmallScale NO: " + reasonWhyNot);
        return false;
    }

    private boolean t(String reasonWhyYEs) {
//        System.out.println("SmallScale YES: " + reasonWhyYEs);
        return true;
    }

    @Override
    public boolean applies() {
//        if (
//            unit.hp() <= (unit.meleeEnemiesNearCount(1.6) <= 1 ? 23 : 36)
//                && unit.friendsNear().combatUnits().havingAntiGroundWeapon().countInRadius(3, unit) == 0
//        ) return true;

        if (allowPendingAttackToContinue()) return f("allowPendingAttackContinue");

        return shouldSmallScaleRetreat();
    }

    private boolean allowPendingAttackToContinue() {
        if (unit.hp() >= 26) {
            if (
                unit.lastActionLessThanAgo(20, Actions.ATTACK_UNIT)
                    && (unit.lastAttackFrameMoreThanAgo(20) || unit.cooldown() <= 5)
            ) return t("recentAttackOrCooldownLow");

            if (
                unit.lastAttackFrameMoreThanAgo(40)
                    && unit.lastUnderAttackLessThanAgo(35)
                    && unit.cooldown() <= 8
            ) return t("longSinceAttackUnderFireCooldownOk");
        }

        return false;
    }

    @Override
    protected Manager handle() {
        AUnit enemy = enemy();
        if (enemy == null) return null;

        if ((new ProtossStartRetreat(unit)).startRetreatingFrom(enemy)) {
//            unit.paintCircleFilled(14, Color.Red);
            return usedManager(this);
        }

        return null;
    }

    private AUnit enemy() {
        return enemies.first();
    }

    public boolean shouldSmallScaleRetreat() {
        if (!unit.isMelee()) return f("notMelee");
        if (unit.isMissionSparta()) return f("missionSparta");

        if (unit.isMissionDefend()) {
            if (unit.isZealot()) {
                if ((new SmallScaleAsZealot(unit)).shouldSmallScaleRetreat()) return t("zealotDefendRetreat");
            }
            if (Enemy.zerg()) {
                if (unit.hp() >= 21) return f("zergHighHp");
                if (unit.allUnitsNear().inRadius(1.2, unit).atLeast(4)) return f("zergManyUnitsNear");
            }
        }

        Selection enemiesCombat = unit.enemiesNear().combatUnits();

        if (enemiesCombat.onlyMelee() && enemiesCombat.inRadius(radius(), unit).empty()) return f("onlyMeleeNoneInRadius");
        if (enemies.inRadius(ProtossSmallScaleEvaluate.RADIUS_LG, unit).count() <= 0) return f("noEnemiesInLargeRadius");
        if (enemiesCombat.groundUnits().empty()) return f("noGroundEnemies");

        double eval = unit.eval();
        if (eval >= 2.5 && unit.hp() >= 35) return f("evalHighHpHigh:" + eval);

        if (unit.friendsInRadiusCount(0.8) >= 2) {
            if (ProtossTooBigBattleToRetreat_asZealot.doNotRetreat(unit)) return f("tooBigBattlePvP");

            if (eval >= 1.2 && unit.hp() >= 35) return f("evalHighHpHigh");
        }

        if (unit.isRanged()) return asRanged(unit, friends, enemies);
        return asMelee(unit, friends, enemies);
    }

    private static double radius() {
        return Enemy.zerg() ? 5.5 : 4;
    }

    protected boolean asMelee(AUnit unit, Selection friends, Selection enemies) {
        if (Enemy.zerg()) return f("dontWhenMeleeVsZerg");

//        if (unit.combatEvalRelative() >= 1.2) return f("");
        if (enemies.inRadius(ProtossSmallScaleEvaluate.RADIUS_LG, unit).count() <= 0) return f("NoEnemiesInLargeRadius2");
        if (unit.hasCooldown() && unit.hasTarget() && unit.target().hp() < unit.hp()) return f("MoreHpThanTarget");

//        if (Enemy.protoss()) {
//            // We're not outnumbered
//            if ((1 + friends.count()) > enemies.count()) return f("MoreFriendsThanEnemies");
//        }

        if (ProtossSmallScaleEvaluate.isOverpoweredByEnemyMelee(unit, friends, enemies)) {
            unit.setTooltip("PSC:A");

            String message = "@" + A.now() + " PSC" + A.digit(ProtossSmallScaleEvaluate.RADIUS_SM)
                + ": " + A.digit(ProtossSmallScaleEvaluate.ourMeleeStrength(unit, friends, ProtossSmallScaleEvaluate.RADIUS_SM))
                + "_vs_" + A.digit(ProtossSmallScaleEvaluate.enemyMeleeStrength(unit, enemies, ProtossSmallScaleEvaluate.RADIUS_SM));
//            System.err.println(message);
            unit.addLog(message);

            return t("overpoweredByEnemyMelee");
        }

//        if (meleeOverpoweredInRadius(unit, friends, enemies, 3.2)) {
//            unit.setTooltip("PSC:B");
//            return t("meleeOverpoweredInRadius");
//        }

        return f("asMeleeGenericNo");
    }

    protected boolean asRanged(AUnit unit, Selection friends, Selection enemies) {
//        if (unit.cooldown() <= 12) return false;

        if (enemies.onlyMelee()) {
            if (unit.shieldDamageAtMost(30)) return false;
            if (unit.meleeEnemiesNearCount(2) == 0) return false;
            if (unit.isMissionDefendOrSparta()) return false;
        }

//        if (unit.combatEvalRelative() <= 1.06 && unit.friendsInRadiusCount(5) < enemies.count()) return t("evalLowFriendsLessThanEnemies");
        if (unit.hp() <= 40 && unit.friendsInRadiusCount(5) < enemies.count()) return t("lowHpOutnumbered");

        return t("asRangedGenericYes");
    }
}
