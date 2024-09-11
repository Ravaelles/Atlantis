package atlantis.units.fix;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Porblematic thing that happens for new units (e.g. Marines) when they come out and just stand outside Barracks.
 */
public class PreventDoNothing extends Manager {
    public PreventDoNothing(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (A.s <= 10) return false;

//        if (A.now() <= 20) return false;
        if (unit.managerLogs().isNotEmpty()) return false;

//        return unit.isActiveManager(DoNothing.class) || unit.isAction(Actions.INIT);

        if (!unit.isStopped()) return false;
        if (unit.action().equals(Actions.INIT)) return true;
//        if (unit.isActiveManager(DoNothing.class)) return true;

        if (unit.isSpecialAction()) return false;
        if (unit.isMissionSparta()) return false;

        return (unit.lastActionMoreThanAgo(30 * 3));
    }

    @Override
    public Manager handle() {
//        if (unit.managerLogs().isEmpty()) {
//            AttackNearbyEnemies manager = new AttackNearbyEnemies(unit);
//            if (manager.invoked(this)) return usedManager(this);
//        }

        if (DoPreventFreezesLogic.handle(unit)) return usedManager(this);

//        System.err.println("OMFG STILL NOTHING! " + unit);

        return null;
    }
}
