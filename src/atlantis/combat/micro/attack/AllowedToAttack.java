package atlantis.combat.micro.attack;

import atlantis.decions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.log.ErrorLog;

public class AllowedToAttack {
    private final AttackNearbyEnemies attackNearbyEnemies;
    private final AUnit unit;

    public AllowedToAttack(AttackNearbyEnemies attackNearbyEnemies, AUnit unit) {
        this.attackNearbyEnemies = attackNearbyEnemies;
        this.unit = unit;
    }

    protected boolean canAttackNow() {
        if (
            unit.isMelee()
                && unit.noCooldown()
                && unit.enemiesNear().inRadius(2, unit).canBeAttackedBy(unit, 0).notEmpty()
        ) return true;

        if ((new AsTerranForbiddenToAttack(unit)).isForbidden()) return false;
        if (!allowedToAttack()) return false;

        // =========================================================

        if (unit.looksIdle() && unit.noCooldown()) return true;

        if (
            unit.lastActionLessThanAgo(70, Actions.RUN_RETREAT)
                && (unit.isMelee() || unit.hasCooldown() || unit.hp() <= 20)
        ) return false;

        boolean shouldRetreat = unit.shouldRetreat();
        if (unit.isMelee() && shouldRetreat) return false;

        if (
            unit.isZergling()
                && (
                (Enemy.protoss() && unit.hp() <= 19) || shouldRetreat
            )
        ) return false;

        if (unit.isMelee()) {
            Selection combatBuildings = Select.ourCombatUnits().buildings();
            if (
                combatBuildings.inRadius(12, unit).notEmpty()
                    && combatBuildings.inRadius(6.8, unit).isEmpty()
            ) return false;
        }

        return true;
    }

    public boolean canAttackEnemiesNow() {
        if (AttackNearbyEnemies.reasonNotToAttack == null) return true;

        return attackNearbyEnemies.defineBestEnemyToAttack(unit) != null;
    }

    public String canAttackEnemiesNowString() {
        return "(" + (canAttackEnemiesNow()
            ? "v"
            : "DONT-" + AttackNearbyEnemies.reasonNotToAttack)
            + ")";
    }

    protected boolean allowedToAttack() {
        if (unit.hasNoWeaponAtAll()) {
            AttackNearbyEnemies.reasonNotToAttack = "NoWeapon";
            return false;
        }

//        if (unit.isTerranInfantry() && Count.medics() >= 2) {
//            if (!unit.medicInHealRange() && (unit.hp() <= 17 || unit.combatEvalRelative() < 1.5)) {
////                if (unit.cooldownRemaining() >= 2) {
//                AttackNearbyEnemies.reasonNotToAttack = "NoMedics";
//                return false;
////                }
//            }
//        }

        // @Problematic - Vultures dont attack from far
//        if (Count.ourCombatUnits() >= 5 && unit.outsideSquadRadius()) {
//            reasonNotToAttack = "Outside";
//            return false;
//        }

        if (
            unit.hasSquad()
                && unit.squad().cohesionPercent() <= 80
                && unit.isAttackingOrMovingToAttack()
        ) {
            if (unit.enemiesNear().ranged().notEmpty() && unit.lastStartedAttackMoreThanAgo(90)) {
                AttackNearbyEnemies.reasonNotToAttack = "Cautious";
                return false;
            }
        }

        Decision decision = unit.mission().permissionToAttack(unit);
        if (decision.notIndifferent()) {
            return decision.toBoolean();
        }

//        Decision decision = unit.mission().allowsToAttackEnemyUnit(unit);
//        if (decision.notIndifferent()) {
//            return decision.toTrueOrFalse();
//        }

        return true;
    }

    protected boolean isValidTargetAndAllowedToAttackUnit(AUnit target) {
        if (target == null || target.position() == null) return false;
        if (!CanAttackCombatBuilding.isAllowed(unit, target)) return false;
        if (unit.isZergling() && target.combatEvalRelative() > 1.7) return false;

        if (!missionAllowsToAttackEnemyUnit(target)) {
            AttackNearbyEnemies.reasonNotToAttack = "MissionForbids" + target.name();
            unit.setTooltipTactical(AttackNearbyEnemies.reasonNotToAttack);
            unit.addLog(AttackNearbyEnemies.reasonNotToAttack);
            return false;
        }

        if (!unit.canAttackTarget(target, false, true)) {
//            if (target.isOverlord()) ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Overlord for " + unit);

            AttackNearbyEnemies.reasonNotToAttack = "InvalidTarget";
            unit.setTooltipTactical(AttackNearbyEnemies.reasonNotToAttack);
            unit.addLog(AttackNearbyEnemies.reasonNotToAttack);

            if (!target.isOverlord()) {
                System.err.println(AttackNearbyEnemies.reasonNotToAttack + " for " + unit + ": " + target + " (" + unit.distTo(target) + ")");
            }
//            A.printStackTrace("Invalid target");
            return false;
        }

        // Prevent units from switching attack of the same unit, to another unit of the same type
//        unit.target().isTank() &&
        AUnit currentTarget = unit.target();
        if (unit.isMelee() && currentTarget != null && !currentTarget.equals(target) && unit.isAttackingOrMovingToAttack()) {
            if (currentTarget.isWorker() || currentTarget.isCombatUnit()) {
                if (unit.distToLessThan(currentTarget, 1.03)) {
                    AttackNearbyEnemies.reasonNotToAttack = "DontSwitch";
                    unit.addLog(AttackNearbyEnemies.reasonNotToAttack);
                    return false;
                }
            }
        }

        if (!target.effVisible()) {
            System.err.println(unit + " got not visible target to attack: " + target);
            return false;
        }

        return true;
    }

    protected boolean missionAllowsToAttackEnemyUnit(AUnit enemy) {
        return unit.mission() == null
            || (unit.isTank() && unit.noCooldown())
            || (unit.isWraith() && unit.isHealthy() && !enemy.isCombatBuilding())
            || unit.mission().allowsToAttackEnemyUnit(unit, enemy);
//            || (unit.isRanged() && enemy.isMelee());
    }
}
