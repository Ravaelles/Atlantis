package atlantis.combat.micro.early.protoss.stick;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.Army;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossForgeExpandStickToCannon extends Manager {
    private AUnit cannon;
    private double dist;
    private Manager submanager = null;

    public ProtossForgeExpandStickToCannon(AUnit unit) {
        super(unit);
    }

//    @Override
//    protected Class<? extends Manager>[] managers() {
//        return new Class[]{
//            ProtossForgeExpandStickToCannonSpecialized.class,
//        };
//    }

    @Override
    public boolean applies() {
        if (!unit.isCombatUnit()) return false;
        if (Count.cannons() <= 0) return false;
        if (unit.shieldWounded() && unit.isRunning()) return false;
        if (unit.lastUnderAttackLessThanAgo(20)) return false;
        int supplyUsed = A.supplyUsed();
        if (supplyUsed >= 80) return false;
        int goons = Count.dragoons();
        int strength = Army.strengthWithoutCB();
        if (goons >= 1 || supplyUsed >= 45) {
            if (goons >= 8) return false;
            if (strength >= 150) return false;
        }

        if (earlyBuildingUnderAttack()) return false;

        submanager = handleSubmanagers();
        if (submanager != null) return true;

        cannon = cannon();
        if (cannon == null) return false;
        dist = unit.distTo(cannon);

        if (dist <= 20 && dist >= 1) {
            if (unit.isMelee() && dist >= 2.0 && Count.ourCombatUnits() <= 5) return true;
            if (unit.hp() <= 50 && dist >= 2.0 && unit.enemiesNear().countInRadius(5.5, unit) > 0) return true;

            if (unit.shieldWound() <= 15 && unit.cooldown() >= 10) return false;
            return unit.cooldown() >= 6;
        }
        if (unit.hp() <= 60 && dist >= 1 && unit.cooldown() >= 1) return true;
        if (unit.cooldown() >= 7 && unit.isMelee() && dist >= 1.5 && dist <= 4) return true;
//        if (unit.hp() <= 60 && unit.cooldown() >= 6) return true;

        if (asMeleeWhenEnemyVeryClose() && dist <= 4.8) return false;
        if (unit.isDragoon() && unit.cooldown() <= 7) return false;

        if (
            goons >= 5
                || (goons >= 3 && strength >= 135)
                || (goons >= 1 && EnemyUnits.discovered().ranged().empty())
        ) return false;

        return isTooFarFromCannon();
    }

    private boolean earlyBuildingUnderAttack() {
        return OurBuildingUnderAttack.notNull()
            && unit.cooldown() <= 8
            && unit.hpPercent() >= 40;
    }

    private boolean allowRoamingGoon() {
        if (unit.shieldWound() >= 20) return false;

        return unit.enemiesThatCanAttackMe(0.9).atMost(1)
            && unit.enemiesNear().ranged().countInRadius(AUnit.NEAR_DIST, unit) <= 2;
    }

    @Override
    public Manager handle() {
        if (submanager != null) return usedManager(this);

        if (unit.cooldown() >= 3 && unit.moveToSafety(Actions.MOVE_FORMATION)) return usedManager(this, "HugSafety");

        if (unit.move(cannon, Actions.MOVE_FORMATION, "HugCannon")) {
            return usedManager(this);
        }

        return null;
    }

    private boolean asMeleeWhenEnemyVeryClose() {
        if (unit.isRanged()) return false;

        AUnit enemy = unit.enemiesNear().inRadius(1.4, unit).nearestTo(unit);
        if (enemy != null && enemy.distTo(cannon) <= 5.8) {
            return true;
        }

        return false;
    }

    private boolean isTooFarFromCannon() {
        return unit.distTo(cannon) > (
            unit.enemiesNear().ranged().empty() ? maxDistWhenNoRanged() : maxDistWhenRangedNear()
        );
    }

    private static double maxDistWhenNoRanged() {
        return 2.8;
    }

    private double maxDistWhenRangedNear() {
        return 1.6 + (unit.isRunning() ? (3 + unit.woundPercent() / 40.0) : 0);
    }

    private AUnit cannon() {
//        AUnit cannon = unit.friendsNear().cannons().nearestTo(unit);
//        if (cannon != null) {
//            return cannon;
//        }

        Selection cannons = Select.ourOfType(AUnitType.Protoss_Photon_Cannon);

        APosition natural = BaseLocations.natural();
        if (natural != null) cannons = cannons.inRadius(AUnit.NEAR_DIST, natural);

        return cannons.mostDistantTo(natural);
    }
}
