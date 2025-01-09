package atlantis.combat.micro.early.protoss.stick;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class ProtossForgeExpandStickToCannonSpecialized extends Manager {
    private AUnit cannon;
    private double dist;

    public ProtossForgeExpandStickToCannonSpecialized(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isCombatUnit()) return false;
        if (Count.cannons() <= 0) return false;
        if (unit.shieldWounded() && unit.isRunning()) return false;
        int supplyUsed = A.supplyUsed();
        if (supplyUsed >= 80) return false;
        int goons = Count.dragoons();
        int strength = Army.strengthWithoutCB();
        if (goons >= 1 || supplyUsed >= 45) {
            if (ResearchSingularityCharge.isResearched()) return false;
            if (goons >= 8) return false;
            if (strength >= 150) return false;
        }

        cannon = cannon();
        if (cannon == null) return false;
        dist = unit.distTo(cannon);

        if (dist <= 25 && dist >= 1.5 && !unit.isRunning()) {
            if (unit.isDragoon()) {
                Decision decision;
                if (Enemy.zerg()) {
                    if ((decision = asGoonVsHydra()).notIndifferent()) return decision.toBoolean();
                    if ((decision = asGoonVsZergling()).notIndifferent()) return decision.toBoolean();
                }

                return allowRoamingGoon();
            }

            if (unit.isZealot()) {
                Decision decision;
                if (Enemy.zerg()) {
                    if ((decision = asZealotVsHydra()).notIndifferent()) return decision.toBoolean();
                    if ((decision = asZealotVsLings()).notIndifferent()) return decision.toBoolean();
                }
                if (Enemy.protoss()) {
                    if ((decision = asZealotVsZealots()).notIndifferent()) return decision.toBoolean();
                }
            }
        }

        return false;
    }

    // =========================================================

    private Decision asZealotVsZealots() {
        Selection zealots = unit.enemiesNear().zealots();
        if (zealots.count() <= 0) return Decision.INDIFFERENT;

        if (
            (dist >= 1 && unit.shields() <= 30)
                || dist >= (1.5 - unit.woundPercent() / 100.0)
                || unit.cooldown() >= 10
                || (unit.cooldown() >= 4 && unit.shields() <= 40)
        ) {
            moveToCannonOrFurther();
            return Decision.TRUE;
        }

        return Decision.FALSE;
    }

    private Decision asZealotVsLings() {
        Selection zealots = unit.enemiesNear().zerglings().inRadius(5.0, unit);
        if (zealots.count() <= 0) return Decision.INDIFFERENT;

        if (
            (dist >= 1 && unit.shields() <= 40)
                || unit.cooldown() >= 10
                || (unit.cooldown() >= 4 && unit.shields() <= 40)
        ) {
            moveToCannonOrFurther();
            return Decision.TRUE;
        }

        return Decision.FALSE;
    }

    private Decision asZealotVsHydra() {
        Selection hydras = unit.enemiesNear().hydras();
        if (hydras.count() <= 0) return Decision.INDIFFERENT;

        if (
            (dist >= 1 && unit.shields() <= 30)
                || dist >= (1.5 - unit.woundPercent() / 100.0)
                || unit.cooldown() >= 10
                || (unit.cooldown() >= 4 && unit.shields() <= 40)
        ) {
            moveToCannonOrFurther();
            return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private Decision asGoonVsHydra() {
        if (!Enemy.zerg()) return Decision.INDIFFERENT;
        Selection hydras = unit.enemiesNear().hydras().inRadius(9, unit);
        if (hydras.count() <= 0) return Decision.INDIFFERENT;

        if (unit.shieldWound() >= 25) {
            if (
                (dist >= 1.3 && unit.shields() <= 40)
                    || dist >= (1.5 - unit.woundPercent() / 100.0)
                    || unit.cooldown() >= 10
                    || (unit.cooldown() >= 4 && unit.shields() <= 40)
            ) {
                moveToCannonOrFurther();
                return Decision.TRUE;
            }
        }

        return Decision.FALSE;
    }

    private Decision asGoonVsZergling() {
        if (!Enemy.zerg()) return Decision.INDIFFERENT;
        Selection lings = unit.enemiesNear().zerglings().inRadius(3.0, unit);
        if (lings.count() <= 1) return Decision.INDIFFERENT;

        if (unit.shieldWound() >= 45) {
            if (
                dist >= (1.5 - unit.woundPercent() / 100.0)
                    || unit.cooldown() >= 10
                    || (unit.cooldown() >= 4 && unit.shields() <= 40)
            ) {
                moveToCannonOrFurther();
                return Decision.TRUE;
            }
        }

        return Decision.FALSE;
    }

    private boolean moveToCannonOrFurther() {
        if (unit.distTo(cannon) >= 0.6 || unit.lastUnderAttackLessThanAgo(15)) {
            if (Count.basesWithUnfinished() >= 2) return unit.moveToNearestBase(Actions.MOVE_FORMATION, cannon);
        }

        return unit.move(cannon, Actions.MOVE_FORMATION, "Stick2Cannon");
    }

    private boolean allowRoamingGoon() {
        if (unit.shieldWound() >= 20) return false;

        return unit.enemiesThatCanAttackMe(0.9).atMost(1)
            && unit.enemiesNear().ranged().countInRadius(AUnit.NEAR_DIST, unit) <= 2;
    }

    @Override
    public Manager handle() {
        if (moveToCannonOrFurther()) return usedManager(this);

        return null;
    }

    private AUnit cannon() {
        Selection cannons = Select.ourOfType(AUnitType.Protoss_Photon_Cannon);

        HasPosition natural = Chokes.natural();
        if (natural != null) cannons = cannons.inRadius(AUnit.NEAR_DIST, natural);

        return cannons.groundFarthestTo(Select.mainOrAnyBuilding());
    }
}
