package atlantis.protoss.shuttle.transports.island_recover;

import atlantis.architecture.Manager;
import atlantis.combat.squad.squads.iota.Iota;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossShuttleRecoverFromIslandsLoadUnits extends Manager {
    private AUnit unitToRecover;

    public ProtossShuttleRecoverFromIslandsLoadUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isShuttle()
            && unit.spaceRemaining() >= 4
            && (unitToRecover = defineOurUnitToRecoverFromIsland()) != null
            && unitToRecover.enemiesNear().combatUnits().groundUnits().countInRadius(9, unitToRecover) == 0;
    }

    @Override
    protected Manager handle() {
//        System.err.println("Recovering unit: " + unitToRecover);
        if (!unit.isMoving() || unit.lastActionMoreThanAgo(40, Actions.LOAD)) {
            if (!unit.unloadedSecondsAgo(7)) {
//                System.err.println("Recovering unit: " + unitToRecover);
                unit.load(unitToRecover);
                unitToRecover.load(unit);
                return usedManager(this, "DropIslandRecover: " + unitToRecover);
            }
        }

        return null;
    }

    private AUnit defineOurUnitToRecoverFromIsland() {
        return Iota.get().units()
            .groundUnits()
            .notLoaded()
            .lastActionMoreThanAgo(30 * 6, Actions.UNLOAD)
            .lastActionMoreThanAgo(30 * 2, Actions.LOAD)
            .nearestTo(unit);
    }
}
