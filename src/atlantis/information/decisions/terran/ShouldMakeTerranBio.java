package atlantis.information.decisions.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.decisions.FocusOnProducingUnits;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class ShouldMakeTerranBio {

    public static String reason;
    private static int infantry;

    public static boolean should() {
        reason = "";

        infantry = Count.infantry();

        // === True ===========================================

        if (A.hasMinerals(680)) return true;

        if (EnemyInfo.isDoingEarlyGamePush()) {
            reason = "EnemyInfo.isDoingEarlyGamePush";
            return true;
        }

        if (OurStrategy.get().goingBio()) {
            if (infantry <= 30) {
                reason = "infantry <= 30";
                return wantsToReturnTrue();
            }

//            if (Count.tanks() <= 1 && infantry <= 15) {
//                return wantsToReturnTrue();
//            }
        }

        int airUnitsAntiGround = EnemyInfo.airUnitsAntiGround();
        if (EnemyStrategy.get().isAirUnits() || airUnitsAntiGround > 0) {
            if (infantry <= (2 + airUnitsAntiGround * 3)) {
                reason = "AntiAir";
                return wantsToReturnTrue();
            }
        }

        // === False ===============================================

        if (FocusOnProducingUnits.haveAnyFocus() && !FocusOnProducingUnits.isFocusedOn(AUnitType.Terran_Marine)) {
            reason = "focusOnOtherUnits";
            return false;
        }

        if (!Decisions.maxFocusOnTanks()) {
            reason = "maxFocusOnTanks";
            return false;
        }

//        if (
//            OurStrategy.get().goingBio()
//                && (infantry <= 18 || AGame.canAffordWithReserved(50, 0))
//        ) {
//            return wantsToReturnTrue();
//        }

        reason = "Generic don't";
        return false;
    }

    private static boolean wantsToReturnTrue() {
        int minInfantry = Enemy.terran() ? 6 : 18;

        if (infantry <= minInfantry || Count.medics() <= 2 || ArmyStrength.weAreMuchWeaker()) return true;

//        if (infantry <= 12) {
//            return true;
//        }

        return AGame.canAffordWithReserved(50, 0);
    }

}
