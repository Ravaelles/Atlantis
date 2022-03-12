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
        if (EnemyInfo.isDoingEarlyGamePush()) {
            return true;
        }

        if (OurStrategy.get().goingBio() && Count.infantry() <= 30) {
            return true;
        }

        return (
            (
                !Decisions.maxFocusOnTanks()
                    &&
                    (
                        (OurStrategy.get().goingBio() || EnemyStrategy.get().isAirUnits())
                            && (Count.infantry() <= 18 || AGame.canAffordWithReserved(50, 0))
                    )
            )
                || !GamePhase.isEarlyGame()
        );
    }

}
