package atlantis.combat.micro.dancing.away.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.always.DragoonAlwaysAvoidEnemy;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;
import bwapi.Color;

public class ForceStopDancingDragoon extends Manager {
    public ForceStopDancingDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isDancingAway()) return false;
        if (unit.isHoldingToShoot()) return false;
        if (unit.isAttacking()) return false;
        if (Enemy.zerg() && unit.hp() <= 22) return false;

        if (unit.isStopped() && unit.hp() >= 45) return t("Stopped");

        if ((new DragoonAlwaysAvoidEnemy(unit)).applies()) return false;

        if (!unit.isStopped()) {
            if (!unit.shotSecondsAgo(3)) return t("NotShot");

            if (unit.lastCommandIssuedAgo() <= 3) return false;
            if (unit.lastActionLessThanAgo(3, Actions.MOVE_DANCE_AWAY)) return false;
        }

        if (unit.enemiesThatCanAttackMe(1.8).notDeadMan().empty()) return t("NoEnemies");

//        if (unit.isStopped()) return true;

        if (Enemy.protoss() && vsProtoss(unit)) return forceStop(unit);
        if (Enemy.zerg() && vsZerg(unit)) return forceStop(unit);
        if (Enemy.terran() && vsTerran(unit)) return forceStop(unit);

        return false;
    }

    private boolean forceStop(AUnit unit) {
        if (unit.isAttacking()) return false;

//        unit.paintCircleFilled(19, Color.Purple);
//        System.out.println(A.now + " ########### Stop dance " + A.digit(unit.nearestEnemyDist()));

        if (unit.isMoving()) {
            unit.setAction(Actions.STOP);

//            if ((new AttackNearbyEnemies(unit)).forceHandled()) {
            if ((new AttackNearbyEnemies(unit)).invokedFrom(this)) {
                return true;
            }
            else {
//                System.err.println(A.now() + " COULD NOT ATTACK / " + AttackNearbyEnemies._failReason);
                unit.stop("ForceStopDancingDragoon");
                unit.setAction(Actions.STOP);
                return true;
            }
        }

        return false;
    }

    private boolean vsProtoss(AUnit unit) {
//        if (unit.isDancingAway()) {
//            unit.paintCircleFilled(22, Color.White);
//        }
        int hp = unit.hp();
        int cooldown = unit.cooldown();

        if (cooldown >= 12 && hp <= 102) return false;
        if (cooldown >= 9 && hp <= 82) return false;
        if (cooldown >= 9 && unit.shieldWound() >= 19) return false;

        Selection enemiesThatCanAttackMe = unit.enemiesThatCanAttackMe(0.75);

        if (cooldown >= 5 && hp <= 42 && enemiesThatCanAttackMe.melee().atLeast(1)) return false;
        if (hp <= 22 && unit.enemiesThatCanAttackMe(2.5).notEmpty()) return false;
        if (hp <= 25 && unit.shotSecondsAgo(2.5) && enemiesThatCanAttackMe.notEmpty()) return false;

        if (enemiesThatCanAttackMe.empty() && hp >= 65 && unit.cooldown() <= 12) {
            return t("P0_Fight");
        }

        if (moreThanOneEnemyCanAttackUs(unit)) return false;

        if (dontStopVsManyEnemyGoonsAround(unit)) {
//            System.err.println(A.now() + " - " + unit.typeWithUnitId() + " - many enemy goons around");
            return false;
        }

        boolean canStop = (cooldown <= 9 && hp >= 62 && unit.eval() >= 0.9)
            || (unit.cooldown() <= 10 && fewEnoughEnemiesNear(unit));

        if (canStop) {
            return t("P_CanStop");
        }

        return false;
    }

    private static boolean moreThanOneEnemyCanAttackUs(AUnit unit) {
        return unit.shieldWound() >= 25
            && unit.eval() <= 4
            && unit.shotSecondsAgo(2)
            && unit.enemiesThatCanAttackMe(0.2).atLeast(2);
    }

    private static boolean dontStopVsManyEnemyGoonsAround(AUnit unit) {
        return unit.shields() <= 45
            && (unit.eval() <= 1.4 || unit.hp() <= 45)
            && unit.shotSecondsAgo(unit.shields() >= 5 ? 1.5 : 3)
            && unit.enemiesNear().dragoons().countInRadius(6, unit) >= 2;
    }

    private boolean vsZerg(AUnit unit) {
        if (unit.hp() <= 42) return false;

        int cooldown = unit.cooldown();

        if (cooldown >= 9 && unit.meleeEnemiesNearCount(OurDragoonRange.range() - 1) >= 1) return false;

        if (
            cooldown >= 10 && unit.shieldWound() <= 35
                && unit.enemiesThatCanAttackMe(0.1).count() <= 3
        ) return t("Z0");

        if (cooldown >= 12 && unit.hp() <= 102) return false;
        if (cooldown >= 9 && unit.hp() <= 82) return false;

        if (cooldown <= 7 && unit.hp() >= 62 && unit.eval() >= 0.9) return t("Z1");
        if (cooldown <= 6 && fewEnoughEnemiesNear(unit)) return t("Z2");

        return false;
    }

    private boolean t(String reason) {
//        System.out.println("FStopDragoon: " + reason + " / " + unit.cooldown());

        return true;
    }

    private boolean vsTerran(AUnit unit) {
        if (unit.hp() <= 42) return false;
        if (unit.cooldown() >= 12 && unit.hp() <= 102) return false;
        if (unit.cooldown() >= 9 && unit.hp() <= 82) return false;
        if (unit.cooldown() <= 4 && unit.hp() >= 102) return false;

        boolean canStop = (unit.cooldown() <= 7 && unit.hp() >= 62 && unit.eval() >= 0.9)
            || fewEnoughEnemiesNear(unit);

//        System.out.println("canStop = " + canStop + " / " + A.now());
        if (canStop) {
            return t("T_CanStop");
        }

        return false;
    }

    private boolean fewEnoughEnemiesNear(AUnit unit) {
        int maxEnemies = unit.hp() >= 62 ? 1 : 0;

        return unit.enemiesThatCanAttackMe(0.8).notDeadMan().atMost(maxEnemies);
    }
}
