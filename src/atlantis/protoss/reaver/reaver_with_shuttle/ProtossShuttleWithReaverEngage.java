package atlantis.protoss.reaver.reaver_with_shuttle;

import atlantis.architecture.Manager;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Protoss_Reaver;

public class ProtossShuttleWithReaverEngage extends Manager {

    private Selection targets;
    private AUnit target;
    private double dist;
    private AUnit reaver;
    private AUnit centerUnit;

    public ProtossShuttleWithReaverEngage(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.loadedUnits().isEmpty()) return false;

        reaver = unit.loadedUnitsGet(Protoss_Reaver);
        if (reaver == null) return false;

        target = defineTarget();
        if (target == null) return false;

        dist = unit.distTo(target);
//        System.err.println("dist = " + dist);

        return true;
    }

    private AUnit defineTarget() {
        centerUnit = Alpha.get().leader();
        if (centerUnit == null) centerUnit = unit;

        targets = centerUnit.enemiesNear().groundUnits().realUnitsAndCombatBuildings().notDeadMan();
//        System.err.println("targets = " + targets.size());

        if (targets.notEmpty()) return targets.nearestTo(reaver);
        else return EnemyUnits.discovered().groundUnits().realUnitsAndCombatBuildings().nearestTo(reaver);
    }

    @Override
    public Manager handle() {
        if (Alpha.count() >= 5 && A.s % 8 <= 1) {
            if (unit.distTo(centerUnit) >= 6) {
                if (unit.move(centerUnit, Actions.MOVE_ENGAGE, "ShuttleEngageLeader")) return usedManager(this);
            }
        }
//        if (unit.distTo(centerUnit) >= 6 || (A.s % 8 <= 1)) {
//            if (unit.move(centerUnit, Actions.MOVE_ENGAGE, "ShuttleEngageLeader")) return usedManager(this);
//        }

        double shotSecondsAgo = unit.shotSecondsAgo();

        AUnit cb = reaver.enemiesNear()
            .combatBuildingsAntiLand()
            .inRadius(10, reaver)
            .nearestTo(reaver);

        if (cb != null) {
            double minDistToCB = 7.5 + unit.woundPercent() / 80.0 + (shotSecondsAgo >= 7 ? -1 : 0);
            if (shouldMoveAwayFromCb(minDistToCB)) {
//                System.err.println(dist + " / minDistToCB = " + minDistToCB);
                reaver.setTooltip("Dont land - CB");
                unit.moveAwayFrom(randomiseCbPosition(cb), 5, Actions.MOVE_AVOID, "ShuttleAvoidCB");
                return usedManager(this);
            }
        }

//        if (dist <= 9.5 + (unit.shotSecondsAgo() <= 5 ? 2 : 0)) {
//        if (dist <= 9.5 + (unit.shotSecondsAgo() <= 5 ? 2 : 0)) {
//        double minDistToTargetToUnload = 9.2 + (shotSecondsAgo >= 3 ? 3 : 0);
        double minDistToTargetToUnload = 9.2;
        if (dist <= minDistToTargetToUnload || unit.hp() <= 31) {
//            System.err.println("TRYING TO UNLOAD (" + dist + ")");
            if (unloadHere()) return usedManager(this, "DELIVERY");
        }
        else {
            HasPosition targetPosition = ShuttleEngagePosition.definePosition(unit, reaver, target);
            if (targetPosition == null) return null;

            if (unit.move(targetPosition, Actions.MOVE_ENGAGE, "ShuttleEngage")) return usedManager(this);
        }

        return null;
    }

    private boolean shouldMoveAwayFromCb(double minDistToCB) {
        return dist <= minDistToCB
            || (reaver.shields() <= 50 && reaver.lastUnderAttackLessThanAgo(30 * 5));
    }

    private boolean unloadHere() {
        APosition walkable = unit.position().makeWalkableAndFreeOfAnyGroundUnits(2, 0.4, reaver);
        if (walkable == null) return false;

        ARegion region = walkable.region();
        if (region == null || !walkable.hasPathTo(target)) return false;

        if (unit.hp() >= 31 && IsNotSafeToUnloadReaverHere.check(unit, reaver)) return false;

        return unit.unload(reaver);
    }

    private HasPosition randomiseCbPosition(AUnit cb) {
        return cb.translateByTiles(A.s % 5 <= 1 ? 0 : randomDelta(), randomDelta());
    }

    private static int randomDelta() {
        int spread = 12;
        return -(spread / 3) + A.randWithSeed(0, spread, A.s % 4);
    }
}
