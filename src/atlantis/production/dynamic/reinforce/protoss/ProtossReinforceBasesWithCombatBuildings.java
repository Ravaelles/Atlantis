package atlantis.production.dynamic.reinforce.protoss;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.reinforce.ReinforceBasesWithCombatBuildings;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ProtossReinforceBasesWithCombatBuildings extends ReinforceBasesWithCombatBuildings {

    private HasPosition nonReinforcedBase;

    @Override
    public boolean applies() {
        if (!A.everyNthGameFrame(53)) return false;
        if (Count.inProductionOrInQueue(Protoss_Photon_Cannon) >= 2) return false;

        nonReinforcedBase = NonReinforcedBases.getNonReinforcedBase();
        return nonReinforcedBase != null;
    }

    @Override
    protected void handle() {
//        if (handleEarlyGameTrouble()) return;

        makeSureIsReinforced(nonReinforcedBase);
    }

//    private boolean handleEarlyGameTrouble() {
//        if (A.everyFrameExceptNthFrame(97)) return false;
//
//        if (ArmyStrength.ourArmyRelativeStrength() <= 70) {
//            if (Count.cannons() <= 2) {
//                (new ReinforceBaseWithCannons(Chokes.mainChoke())).invokeCommander();
//                return true;
//            }
//        }
//
//        return false;
//    }

    @Override
    protected void makeSureIsReinforced(HasPosition position) {
        (new ReinforceBaseWithCannons(position)).invokeCommander();
    }
}
