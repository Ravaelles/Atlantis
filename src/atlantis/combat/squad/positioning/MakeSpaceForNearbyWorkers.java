package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class MakeSpaceForNearbyWorkers extends Manager {
    private AChoke choke;

    public MakeSpaceForNearbyWorkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isLoaded()) return false;
        if (unit.isMissionAttackOrGlobalAttack()) return false;
        if (unit.enemiesNear().inRadius(12, unit).havingWeapon().notEmpty()) return false;
        if (unit.isMissionDefend() && EnemyWhoBreachedBase.notNull()) return false;
        if (unit.friendsNear().groundUnits().nonBuildings().countInRadius(1, unit) <= 1) return false;
        if (unit.friendsNear().workers().inRadius(4, unit).empty()) return false;
        if (unit.enemiesNearInRadius(12) > 0) return false;
        if (Select.ourBases().inRadius(5, unit).notEmpty()) return false;
        if (A.seconds() % 6 <= 3) return false;
        if (unit.friendsNear().buildings().empty()) return false;

        if (
            (choke = Chokes.nearestChoke(unit, "MAIN")) != null
                && choke.width() >= 5
                && choke.distTo(unit) >= 6
        ) return false;

        return true;
    }

    protected Manager handle() {
        AUnit nearWorker = Select.ourWorkers().notGathering().inRadius(1.7, unit).first();

        if (nearWorker != null) {
            unit.setTooltipAndLog("Space4Worker");

            if (asTerran()) return usedManager(this);
            else {
                if (unit.moveAwayFrom(nearWorker, 3, Actions.MOVE_SPACE, "Space4W01rker")) {
                    return usedManager(this, "Space4W0rker");
                }
            }
        }

        return null;
    }

    private boolean asTerran() {
        if (unit.isTankSieged()) {
            if (TerranTank.wantsToUnsiege(unit)) {
                return true;
            }
        }

        return false;
    }
}
