package atlantis.combat.retreating.protoss.big_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossFullRetreat extends Manager {
    public ProtossFullRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionAttack()) return false;
        if (OurArmy.strength() >= 500) return false;
        if (unit.combatEvalRelative() >= 1.6) return false;
        if (unit.enemiesNear().combatUnits().empty()) return false;
        if (unit.enemiesNear().combatUnits().atMost(3)) return false;
        if (unit.friendsNear().combatUnits().atLeast(15)) return false;

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
            - combaBuildingPenalty(unit);

        return evalRelative <= 0.95;
    }

    private double combaBuildingPenalty(AUnit unit) {
        Selection combatBuildings = EnemyUnits.discovered().buildings().combatBuildingsAnti(unit);
        if (combatBuildings.empty()) return 0;

        return combatBuildings.inRadius(17, unit).count() / 2.0;
    }

    @Override
    protected Manager handle() {
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
