package atlantis.strategy;

import atlantis.util.A;

public class GamePhase {

    public static boolean isEarlyGame() {
        return A.seconds() <= 400;
    }

    public static boolean isMidGame() {
        return 400 < A.seconds() && A.seconds() <= 800;
    }

    public static boolean isLateGame() {
        return A.seconds() < 800;
    }

}
