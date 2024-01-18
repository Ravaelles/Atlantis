package atlantis.units.special;

import atlantis.architecture.Manager;
import atlantis.architecture.generic.DoNothing;
import atlantis.combat.advance.AdvanceStandard;
import atlantis.combat.advance.focus.TooFarFromFocusPoint;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.attack.MissionAttack;
import atlantis.combat.squad.TerranSquadCohesionManager;
import atlantis.combat.squad.positioning.SquadCohesion;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.log.ErrorLog;

/**
 * Porblematic thing that happens for new units (e.g. Marines) when they come out and just stand outside Barracks.
 */
public class PreventDoNothing extends Manager {
    public PreventDoNothing(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isActiveManager(DoNothing.class)
            && unit.lastPositionChangedMoreThanAgo(33)
            && (unit.isStopped() || unit.lastActionMoreThanAgo(11));
    }

    @Override
    public Manager handle() {
//        ErrorLog.printMaxOncePerMinute(A.now() + " PreventDoNothing for " + unit);

        if ((new AttackNearbyEnemies(unit)).forceHandle() != null) return usedManager(this);

        if ((new AdvanceStandard(unit)).forceHandle() != null) return usedManager(this);

//        if (A.everyNthGameFrame(3)) {
//            if ((new TooFarFromFocusPoint(unit)).forceHandle() != null) return usedManager(this);
//        }

        return null;
    }
}
