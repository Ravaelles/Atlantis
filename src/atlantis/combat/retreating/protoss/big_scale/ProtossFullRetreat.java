package atlantis.combat.retreating.protoss.big_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.combat.squad.positioning.protoss.ProtossTooLonely;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossFullRetreat extends Manager {
    public ProtossFullRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionAttack()) return false;
        Selection enemies = unit.enemiesNear().combatUnits();

        if (enemies.empty()) return false;
        if (OurArmy.strength() >= 600) return false;
        if (unit.combatEvalRelative() >= 1.2) return false;

        if (A.s <= 600 && (Enemy.protoss() || Enemy.zerg()) && EnemyUnits.discovered().ranged().empty()) {
            if (enemies.canAttack(unit, 2.8 + unit.woundPercent() / 100.0).empty()) return false;
        }

        if (unit.enemiesNear().combatBuildingsAntiLand().empty()) {
            if (unit.combatEvalRelative() >= 2.6) return false;
            if (enemies.atMost(2)) return false;
            if (unit.friendsNear().combatUnits().atLeast(10)) return false;
            if (unit.shieldDamageAtMost(19) && unit.enemiesNear().ranged().empty()) return false;
        }

//        if (
//            enemies.onlyMelee()
//                && unit.combatEvalRelative() >= 0.8
//                && !(new ProtossSmallScaleRetreat(unit).applies())
//        ) {
//            unit.addLog("StillFightSS");
//            return false;
//        }

        AUnit base = Select.naturalOrMain();
        if (base == null || (unit.hp() >= 33 && unit.cooldown() <= 5 && base.distTo(unit) <= 8)) return false;

        if (unit.isMissionDefendOrSparta()) {
            AChoke mainChoke = Chokes.mainChoke();
            if (
                mainChoke != null
                    && unit.distTo(mainChoke) >= 2
                    && unit.distToNearestChokeCenter() <= 5
                    && base.distTo(unit) <= 25
            ) return false;

            if (!Enemy.protoss()) {
                AChoke natural = Chokes.natural();
                APosition naturalBase = DefineNaturalBase.natural();
                if (
                    natural != null
                        && naturalBase != null
                        && natural.distTo(unit) >= 2.5
                        && natural.distTo(unit) <= 8
                        && naturalBase.distTo(unit) <= 10
                ) return false;
            }
        }

        double evalRelative = unit.combatEvalRelative()
            + (unit.isMissionDefendOrSparta() ? 0 : (unit.distToNearestChokeLessThan(4) ? -0.4 : 0))
            + (unit.lastRetreatedAgo() <= 30 * 4 ? -0.25 : 0)
//            + (unit.lastStartedRunningLessThanAgo(30 * 4) ? 0.1 : 0)
            + (unit.distToMain() <= 20 ? +0.15 : 0)
            + (unit.lastUnderAttackLessThanAgo(30 * 4) ? -0.05 : 0)
//            + combatBuildingPenalty(unit)
            + enemyZerglingBonus(unit);

        return evalRelative <= 0.95;
    }

    @Override
    protected Manager handle() {
        if (unit.hp() >= 30 && unit.friendsNear().bases().inRadius(6, unit).notEmpty()) {
            return null;
        }

        ProtossTooLonely tooLonely = new ProtossTooLonely(unit);
        if (tooLonely.applies() && tooLonely.forceHandle() != null) return usedManager(this);

        if ((new ProtossStartRetreat(unit)).startRetreatingFrom(enemy())) {
//            unit.paintCircleFilled(14, Color.Red);
            if (unit.isLeader()) RetreatManager.GLOBAL_RETREAT_COUNTER++;

            unit.addLog("PFull");
            return usedManager(this);
        }

        return null;
    }

    /**
     * Lower value of enemy zerglings.
     */
    private double enemyZerglingBonus(AUnit unit) {
        if (unit.friendsNear().inRadius(3, unit).atMost(1)) return 0;

        return unit.enemiesNear().inRadius(8, unit).ofType(AUnitType.Zerg_Zergling).count() * 0.3;
    }
//
//    private double combatBuildingPenalty(AUnit unit) {
//        Selection combatBuildings = EnemyUnits.discovered().buildings().combatBuildingsAnti(unit);
//        if (combatBuildings.empty()) return 0;
//
//        int basePenalty = Alpha.count() <= 25 ? 4 : 0;
//        basePenalty += Alpha.get().leader().lastRetreatedAgo() <= 30 * 15 ? 2 : 0;
//
//        return basePenalty + combatBuildings.inRadius(17, unit).count() / 1.5;
//    }

    private AUnit enemy() {
        return unit.enemiesNear().groundUnits().combatUnits().nearestTo(unit);
    }
}
