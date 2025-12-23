package atlantis.combat.micro.avoid.buildings;

import atlantis.combat.micro.avoid.buildings.protoss.PvZDontAvoidCB;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.util.We;

import static atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingClose.f;
import static atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingClose.t;

public class AllowAvoidingCB {
    public static boolean allowed(AUnit unit, AUnit combatBuilding, AUnit leader) {
        if (We.protoss()) return allowedAsProtoss(unit, combatBuilding, leader);

        return false;
    }

    private static boolean allowedAsProtoss(AUnit unit, AUnit combatBuilding, AUnit leader) {
        if (unit.isAir()) return t("AirAlways");

        if (
            Army.strengthWithoutOurCB() >= 300
                && Alpha.evalOr(0) >= 5
                && Alpha.alphaLeader() != null
                && Alpha.alphaLeader().lastRetreatedAgo() >= 25
        ) return f("StrongArmyNoCBAvoid");

        if (A.supplyUsed(unit.eval() >= 3 ? 190 : 196)) {
            if (unit.lastStoppedRunningMoreThanAgo(30 * 12) && leader.lastStoppedRunningMoreThanAgo(30 * 12)) {
                return f("RichToss");
            }
        }

        if (PvZDontAvoidCB.dontAvoid(unit, combatBuilding)) return f("VsZergDontAvoidCB");

        return true;
    }
}
