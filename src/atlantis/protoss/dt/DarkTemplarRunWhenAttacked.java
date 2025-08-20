package atlantis.protoss.dt;

import atlantis.architecture.Manager;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DarkTemplarRunWhenAttacked extends Manager {
    public DarkTemplarRunWhenAttacked(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.lastUnderAttackLessThanAgo(30 * 5)
            && unit.eval() <= 4
            && unit.enemiesThatCanAttackMe(5).notEmpty();
    }

    @Override
    public Manager handle() {
        if (unit.hp() >= 80) {
            AUnit leader = Alpha.alphaLeader();
            if (leader != null && unit.distTo(leader) >= 4) {
                if (unit.move(leader, Actions.RUN_ENEMY)) {
                    return usedManager(this, "DT-RunToLeader");
                }
            }
        }

        if (unit.hp() <= 80) {
            APosition center = unit.enemiesThatCanAttackMe(5).center();
            if (unit.moveAwayFrom(center, 6, Actions.RUN_ENEMY)) {
                return usedManager(this, "DT-RunFrom");
            }
        }

        if (unit.moveToSafety(Actions.RUN_ENEMY)) {
            return usedManager(this, "DT-RunSafety");
        }

        return null;
    }
}
