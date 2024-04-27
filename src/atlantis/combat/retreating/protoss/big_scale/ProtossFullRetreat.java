package atlantis.combat.retreating.protoss.big_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.positioning.too_lonely.ProtossTooLonely;
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

public class ProtossFullRetreat extends Manager {
    public ProtossFullRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionAttack()) return false;
        if (unit.enemiesNear().combatUnits().empty()) return false;
        if (unit.enemiesNear().combatBuildingsAntiLand().empty()) {
            if (OurArmy.strength() >= 700) return false;
            if (unit.combatEvalRelative() >= 2.6) return false;
            if (unit.enemiesNear().combatUnits().atMost(3)) return false;
            if (unit.friendsNear().combatUnits().atLeast(15)) return false;
        }

        AUnit base = Select.naturalOrMain();
        if (base == null || base.distTo(unit) <= 20) return false;

        if (unit.isMissionDefendOrSparta()) {
            AChoke mainChoke = Chokes.mainChoke();
            if (
                mainChoke != null
                    && mainChoke.distTo(unit) >= 2.5
                    && mainChoke.distTo(unit) <= 8
                    && base.distTo(unit) <= 25
            ) return false;

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

        double evalRelative = unit.combatEvalRelative()
            - (unit.isMissionDefendOrSparta() ? 0 : (unit.distToNearestChokeLessThan(4) ? 0.35 : 0))
            - (unit.lastRetreatedAgo() <= 100 ? 0.2 : 0)
            - (unit.lastStartedRunningLessThanAgo(30 * 4) ? 0.05 : 0)
            - (unit.distToMain() <= 20 ? -0.1 : 0)
            - (unit.lastUnderAttackLessThanAgo(30 * 4) ? 0.05 : 0)
            - combatBuildingPenalty(unit)
            + enemyZerglingBonus(unit);

        return evalRelative <= 0.95;
    }

    /**
     * Lower value of enemy zerglings.
     */
    private double enemyZerglingBonus(AUnit unit) {
        if (unit.friendsNear().inRadius(3, unit).atMost(1)) return 0;

        return unit.enemiesNear().inRadius(8, unit).ofType(AUnitType.Zerg_Zergling).count() * 0.3;
    }

    private double combatBuildingPenalty(AUnit unit) {
        Selection combatBuildings = EnemyUnits.discovered().buildings().combatBuildingsAnti(unit);
        if (combatBuildings.empty()) return 0;

        int basePenalty = Alpha.count() <= 25 ? 4 : 0;
        basePenalty += Alpha.get().leader().lastRetreatedAgo() <= 30 * 15 ? 2 : 0;

        return basePenalty + combatBuildings.inRadius(17, unit).count() / 1.5;
    }

    @Override
    protected Manager handle() {
        ProtossTooLonely tooLonely = new ProtossTooLonely(unit);
        if (tooLonely.applies() && tooLonely.forceHandle() != null) return usedManager(this);

        if ((new ProtossStartRetreat(unit)).startRetreatingFrom(enemy())) {
//            unit.paintCircleFilled(14, Color.Red);
            unit.addLog("PFull");
            return usedManager(this);
        }

        return null;
    }

    private AUnit enemy() {
        return unit.enemiesNear().groundUnits().combatUnits().nearestTo(unit);
    }
}
