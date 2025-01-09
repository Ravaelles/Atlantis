
package atlantis.protoss.ht;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.TechType;

public class ProtossHighTemplar extends Manager {

    public ProtossHighTemplar(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(AUnitType.Protoss_High_Templar);
    }

    @Override
    protected Manager handle() {
        if (dontDisturb()) return usedManager(this);
        if (tryMeldingIntoArchon()) return usedManager(this);

        if (AGame.everyNthGameFrame(7)) {
            if (new UsePsionicStorm(unit).handle()) return usedManager(this);
        }

        if (followArmy()) return usedManager(this);

        return null;
    }

    // =========================================================

    private boolean dontDisturb() {

        // Wants to Warp Archon
        if (
            unit.lastTechUsed() != null
                && unit.lastActionLessThanAgo(50, Actions.USING_TECH)
                && TechType.Archon_Warp.name().equals(unit.lastTechUsed().name())
                && unit.lastTechUnit().isAlive()
        ) {
            unit.setTooltipTactical("Sex & Archon");
            return true;
        }

        // Is target of Archon Warp
        for (AUnit otherHT : Select.ourOfType(AUnitType.Protoss_High_Templar).inRadius(3, unit).list()) {
            if (unit.equals(otherHT.target()) && unit.lastTechUsedAgo() <= 90) {
                unit.setTooltipTactical("Lets get it on");
                return true;
            }
        }

        if (unit.lastActionLessThanAgo(40, Actions.USING_TECH)) {
            unit.setTooltipTactical(unit.lastTechUsed().name() + "...");
            return true;
        }

        return false;
    }

    private boolean followArmy() {
        if (unit.hp() <= 16) return false;

        AUnit leader = Alpha.get().leader();
        if (leader != null) {
            if (leader.friendsNear().groundUnits().inRadius(0.3, unit).atLeast(3)) {
                return unit.moveAwayFrom(
                    Select.our().exclude(unit).nearestTo(unit),
                    3,
                    Actions.MOVE_FORMATION, "Stacked"
                );
            }

            if (
                leader.distTo(unit) > 2
                    && unit.move(leader, Actions.MOVE_FOLLOW, "Follow army", true)
            ) {
                return true;
            }
        }

        return false;
    }

    private boolean tryMeldingIntoArchon() {
        if (unit.energy() > 65 && unit.woundPercent() < 60) return false;

        Units lowEnergyHTs = new Units();
        for (AUnit other : Select.ourOfType(AUnitType.Protoss_High_Templar).inRadius(8, unit).list()) {
            if (other.energy() <= 70 || unit.woundPercent() >= 60) {
                lowEnergyHTs.addUnitWithValue(other, other.distTo(unit));
            }
        }

        AUnit closestOtherHT = lowEnergyHTs.unitWithLowestValue();
        if (closestOtherHT != null) {
//            if (closestOtherHT.distTo(unit) <= 0.9) {
            unit.useTech(TechType.Archon_Warp, closestOtherHT);

            unit.setTooltipTactical("WarpArchon");
            closestOtherHT.setTooltipTactical("OhArchon");
//                GameSpeed.changeSpeedTo(10);
//                CameraCommander.centerCameraOn();
//            }
//            else {
//                if (!unit.isMoving() && closestOtherHT.lastActionMoreThanAgo(90, UnitActions.USING_TECH)) {
//                    unit.useTech(TechType.Archon_Warp, closestOtherHT);
//                    unit.move(closestOtherHT, UnitActions.MOVE, "WarpArchon");
//                }
//                if (!closestOtherHT.isMoving() && closestOtherHT.lastActionMoreThanAgo(90, UnitActions.USING_TECH)) {
//                    closestOtherHT.useTech(TechType.Archon_Warp, unit);
//                    closestOtherHT.move(UnitActions.MOVE, "WarpArchon");
//                }
//            }
            return true;
        }

        return false;
    }


}
