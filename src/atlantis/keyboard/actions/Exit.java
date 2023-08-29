package atlantis.keyboard.actions;

import atlantis.game.AGame;

public class Exit {
    public static void handle() {
        System.out.println();
        System.out.println("Exit requested by the user");
        AGame.exit();
    }
}