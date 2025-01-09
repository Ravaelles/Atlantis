package atlantis.combat.retreating.protoss.small_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.combat.retreating.protoss.should.ProtossShouldRetreat;
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

        friends = ProtossShouldRetreat.friends(unit);
        enemies = ProtossShouldRetreat.enemies(unit);
    }

    @Override
    public boolean applies() {
//        if (
//            unit.hp() <= (unit.meleeEnemiesNearCount(1.6) <= 1 ? 23 : 36)
//                && unit.friendsNear().combatUnits().havingAntiGroundWeapon().countInRadius(3, unit) == 0
//        ) return true;

        if (allowPendingAttackToContinue()) return false;

        return shouldSmallScaleRetreat();
    }

    private boolean allowPendingAttackToContinue() {
        if (unit.hp() >= 26) {
            if (
                unit.lastActionLessThanAgo(20, Actions.ATTACK_UNIT)
                    && (unit.lastAttackFrameMoreThanAgo(20) || unit.cooldown() <= 5)
            ) return true;

            if (
                unit.lastAttackFrameMoreThanAgo(40)
                    && unit.lastUnderAttackLessThanAgo(35)
                    && unit.cooldown() <= 8
            ) return true;
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
        if (!unit.isMelee()) return false;
        if (unit.isMissionSparta()) return false;

        if (unit.isMissionDefend()) {
            if (unit.isZealot()) {
                if ((new SmallScaleAsZealot(unit)).shouldSmallScaleRetreat()) return true;
            }
            if (Enemy.zerg()) {
                if (unit.hp() >= 21) return false;
                if (unit.allUnitsNear().inRadius(1.2, unit).atLeast(4)) return false;
            }
        }

        Selection enemiesCombat = unit.enemiesNear().combatUnits();

        if (enemiesCombat.onlyMelee() && enemiesCombat.inRadius(radius(), unit).empty()) return false;
        if (enemies.inRadius(ProtossSmallScaleEvaluate.RADIUS_LG, unit).count() <= 0) return false;

        if (enemiesCombat.groundUnits().empty()) return false;
        if (unit.eval() >= 1.1 && unit.hp() >= 24) return false;
        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return false;

        if (unit.isRanged()) return asRanged(unit, friends, enemies);
        return asMelee(unit, friends, enemies);
    }

    private static double radius() {
        return Enemy.zerg() ? 5.5 : 4;
    }


    protected boolean asMelee(AUnit unit, Selection friends, Selection enemies) {
//        if (unit.combatEvalRelative() >= 1.2) return false;
        if (enemies.inRadius(ProtossSmallScaleEvaluate.RADIUS_LG, unit).count() <= 0) return false;
        if (unit.hasCooldown() && unit.hasTarget() && unit.target().hp() < unit.hp()) return false;

        if (Enemy.protoss()) {
            // We're not outnumbered
            if ((1 + friends.count()) > enemies.count()) return false;
        }

        if (ProtossSmallScaleEvaluate.isOverpoweredByEnemyMelee(unit, friends, enemies)) {
            unit.setTooltip("PSC:A");

            String message = "@" + A.now() + " PSC" + A.digit(ProtossSmallScaleEvaluate.RADIUS_SM)
                + ": " + A.digit(ProtossSmallScaleEvaluate.ourMeleeStrength(unit, friends, ProtossSmallScaleEvaluate.RADIUS_SM))
                + "_vs_" + A.digit(ProtossSmallScaleEvaluate.enemyMeleeStrength(unit, enemies, ProtossSmallScaleEvaluate.RADIUS_SM));
//            System.err.println(message);
            unit.addLog(message);

            return true;
        }

//        if (meleeOverpoweredInRadius(unit, friends, enemies, 3.2)) {
//            unit.setTooltip("PSC:B");
//            return true;
//        }

        return false;
    }

    protected boolean asRanged(AUnit unit, Selection friends, Selection enemies) {
//        if (unit.cooldown() <= 12) return false;

        if (enemies.onlyMelee()) {
            if (unit.shieldDamageAtMost(30)) return false;
            if (unit.meleeEnemiesNearCount(2) == 0) return false;
            if (unit.isMissionDefendOrSparta()) return false;
        }

//        if (unit.combatEvalRelative() <= 1.06 && unit.friendsInRadiusCount(5) < enemies.count()) return true;
        if (unit.hp() <= 40 && unit.friendsInRadiusCount(5) < enemies.count()) return true;

        return false;
    }
}
