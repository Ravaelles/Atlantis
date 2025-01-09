package starengine.engine_game;

import atlantis.units.select.Select;
import starengine.StarEngine;

public class StarEngineGame {
    private final StarEngine starEngine;
    private boolean gameEnd = false;
    private boolean weWon = false;

    // =========================================================

    public StarEngineGame(StarEngine starEngine) {
        this.starEngine = starEngine;
    }

    // =========================================================

    public boolean checkForGameEnd() {
//        System.err.println(Select.our().havingAtLeastHp(1).count()
//            + " / " + Select.enemy().havingAtLeastHp(1).count());

        if (Select.our().havingAtLeastHp(1).empty()) {
            gameEnd = true;
            weWon = false;
        }
        else if (Select.enemy().havingAtLeastHp(1).empty()) {
            gameEnd = true;
            weWon = true;
        }

        if (gameEnd) {
            System.out.println(
                "### StarEngine ###\n" +
                    "GAME OVER, " + (weWon ? "VICTORY!" : "DEFEAT!") + "\n"
            );
            starEngine.testClass().setShouldQuitGameLoopNow(true);
            starEngine.closeIfNeeded();
        }

        return gameEnd;
    }

    // =========================================================

    public boolean isGameEnd() {
        return gameEnd;
    }

    public boolean weWon() {
        return weWon;
    }
}
