package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class MakeSpaceForNearbyWorkers extends Manager {
    public MakeSpaceForNearbyWorkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMissionAttack()) return false;

        if (unit.friendsNear().groundUnits().nonBuildings().countInRadius(1, unit) <= 1) return false;

        if (unit.enemiesNearInRadius(12) > 0) {
            return false;
        }

        if (A.seconds() % 6 <= 3) return false;

        AChoke choke = Chokes.nearestChoke(unit);
        if (choke != null && choke.distTo(unit) >= 5) return false;

        return true;
    }

    public Manager handle() {
        AUnit nearWorker = Select.ourWorkers().inRadius(1.5, unit).first();

        if (nearWorker != null) {
            unit.setTooltipAndLog("Space4Worker");
            if (unit.isTankSieged()) {
                if (TerranTank.wantsToUnsiege(unit)) {
                    return usedManager(this);
                }
            }
            else {
                AUnit main = Select.main();
                if (main != null && main.distToMoreThan(unit, 5)) {
                    unit.move(main, Actions.MOVE_SPACE, "Space4Worker");
                    return usedManager(this);
                }
            }
        }

        return null;
    }
}
