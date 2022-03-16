package atlantis.information.decisions.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.select.Count;

public class ShouldBuildBio {

    public static boolean should() {
        int infantry = Count.infantry();

        if (EnemyInfo.isDoingEarlyGamePush()) {
            return wantsToReturnTrue();
        }

        if (OurStrategy.get().goingBio()) {
            if (Count.tanks() <= 1 && infantry <= 15) {
                return wantsToReturnTrue();
            }

            if (infantry <= 30) {
                return wantsToReturnTrue();
            }
        }

        if (
            (
                !Decisions.maxFocusOnTanks()
                    &&
                    (
                        (OurStrategy.get().goingBio() || EnemyStrategy.get().isAirUnits())
                            && (infantry <= 18 || AGame.canAffordWithReserved(50, 0))
                    )
            )
                || !GamePhase.isEarlyGame()
        ) {
            return wantsToReturnTrue();
        }

        return false;
    }

    private static boolean wantsToReturnTrue() {
        int infantry = Count.infantry();

        if (infantry <= 12) {
            return true;
        }

        return AGame.canAffordWithReserved(60, 0);
    }

}
