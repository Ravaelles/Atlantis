package atlantis.keyboard.actions;

import atlantis.game.A;
import atlantis.game.AGame;

public class Exit {
    public static void handle() {
        A.println("\nExit was requested manually. Cleaning up...");
        AGame.exit();
    }
}
