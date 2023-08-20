package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.wraith.MoveAsLooksIdle;
import atlantis.combat.targeting.ATargeting;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.cache.Cache;

public class AttackNearbyEnemies extends Manager {
    private static Cache<AUnit> cache = new Cache<>();
    private static Cache<Object> cacheObject = new Cache<>();
    public static String reasonNotToAttack;
    private static ProcessAttackUnit processAttackUnit;
    private final AllowedToAttack allowedToAttack;

    // =========================================================

    public AttackNearbyEnemies(AUnit unit) {
        super(unit);
        processAttackUnit = (new ProcessAttackUnit(unit));
        allowedToAttack = (new AllowedToAttack(this, unit));
    }

    public static double maxDistToAttack(AUnit unit) {
        return unit.isAir() ? 999 : 25;
    }

    // =========================================================

    @Override
    public boolean applies() {
        if (unit.manager().equals(this) && unit.looksIdle() && unit.enemiesNear().empty()) return false;
        if (unit.enemiesNear().canBeAttackedBy(unit, 15).empty()) return false;
        if (unit.isWraith()) {
            if (
                unit.lastActionMoreThanAgo(10)
                    && unit.enemiesNear().notEmpty()
                    && A.chance(10)
                    && unit.lastActionMoreThanAgo(30 * 3, Actions.MOVE_SPECIAL)
            ) {
                MoveAsLooksIdle moveAsLooksIdle = new MoveAsLooksIdle(unit);
                moveAsLooksIdle.invoke();
                usedManager(moveAsLooksIdle);
                return false;
            }

//            if (unit.looksIdle() && unit.lastActionMoreThanAgo(20) && A.chance(10)) return true;
            if (A.chance(5)) return true;

            return false;
//            if (unit.lastAttackFrameMoreThanAgo(70)) return false;
//
//            if (A.chance(50)) return false;
        }


        return unit.hasAnyWeapon();
    }

    protected Manager handle() {
        if (this.equals(unit.manager()) && justHandledRecently() && !unit.looksIdle()) {
            return usedManager(this);
        }

        if (handleAttackNearEnemyUnits()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean justHandledRecently() {
        return unit.lastActionLessThanAgo(8, Actions.ATTACK_UNIT)
            || unit.lastActionLessThanAgo(8, Actions.MOVE_ATTACK);
    }

    /**
     * Selects the best enemy unit and issues attack order.
     *
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public boolean handleAttackNearEnemyUnits() {
//        return (boolean) cacheObject.getIfValid(
//            "handleAttackNearEnemyUnits: " + unit.id(),
//            4,
//            () -> {

        if (!applies()) return false;

        if (unit.target() != null) {
            if (!unit.mission().allowsToAttackEnemyUnit(unit, unit.target())) return false;
        }

        if (!allowedToAttack.canAttackNow()) return false;

        if (unit.isAttacking() && (
            unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)
                || unit.lastActionLessThanAgo(5, Actions.MOVE_ATTACK)
        )) {
            if (unit.target() != null && unit.hasPosition() && (!unit.looksIdle() || unit.hasCooldown())) {
                return true;
            }

//            if (unit.target() != null && unit.hasPosition() && !unit.looksIdle()) {
//                if (unit.mission().allowsToAttackEnemyUnit(unit, unit.target())) {
//                    return true;
//                }
//            }
        }

        // =========================================================

        AUnit enemy = (new AttackNearbyEnemies(unit)).defineEnemyToAttackFor();

        if (enemy == null || !unit.canAttackTarget(enemy)) {
            return false;
        }

        // =========================================================

        if (unit.mission().allowsToAttackEnemyUnit(unit, enemy)) {
            return processAttackUnit.processAttackOtherUnit(enemy);
        }
        else {
            return false;
        }

//            }
//        );
    }

    public String canAttackEnemiesNowString() {
        return allowedToAttack.canAttackEnemiesNowString();
    }

    // =========================================================

    protected AUnit defineEnemyToAttackFor() {
        return cache.getIfValid(
            "defineEnemyToAttackFor",
            2,
            () -> {
                reasonNotToAttack = null;

                AUnit enemy = bestTargetToAttack();
//                System.out.println("enemy = " + enemy);

                if (enemy == null) {
                    return null;
                }
                if (!allowedToAttack.isValidTargetAndAllowedToAttackUnit(enemy)) {
//                    System.out.println("Not allowed to attack: " + enemy + " (" + reasonNotToAttack + ")");
                    return null;
                }

                return enemy;
            }
        );
    }

    protected AUnit bestTargetToAttack() {
        return ATargeting.defineBestEnemyToAttackFor(unit, maxDistToAttack(unit));
    }
}
