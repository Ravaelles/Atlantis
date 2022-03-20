package atlantis.information.decisions.terran;

import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.select.Count;

public class ShouldMakeTerranBio {

    private static int infantry;

    public static boolean should() {
        infantry = Count.infantry();

        if (EnemyInfo.isDoingEarlyGamePush()) {
            return wantsToReturnTrue();
        }

        if (OurStrategy.get().goingBio()) {
            if (infantry <= 30) {
                return wantsToReturnTrue();
            }

//            if (Count.tanks() <= 1 && infantry <= 15) {
//                return wantsToReturnTrue();
//            }
        }

        if (
            !GamePhase.isEarlyGame()
                || (
                !Decisions.maxFocusOnTanks()
                    &&
                    (
                        (OurStrategy.get().goingBio() || EnemyStrategy.get().isAirUnits())
                            && (infantry <= 18 || AGame.canAffordWithReserved(50, 0))
                    )
            )
        ) {
            return wantsToReturnTrue();
        }

        return false;
    }

    private static boolean wantsToReturnTrue() {
        if (infantry <= 3) {
            return true;
        }

//        if (infantry <= 12) {
//            return true;
//        }

        return AGame.canAffordWithReserved(50, 0);
    }

}
