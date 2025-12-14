package atlantis.cherryvis.generic;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.APlayer;
import atlantis.map.AMap;
import atlantis.cherryvis.ACherryVisConfig;

public class ACherryVis_GameSummary {
    private ACherryVisConfig config;

    public ACherryVis_GameSummary(ACherryVisConfig config) {
        this.config = config;
    }

    public void saveToFile() {
        APlayer playerUs = AGame.playerUs();
        APlayer playerEnemy = AGame.enemy();

        String content = "{\n" +
            "  \"cp_final_bo\": \"\",\n" +
            "  \"cp_opening_bo\": \"\",\n" +
            "  \"draw\": false,\n" +
            "  \"game_duration_frames\": " + A.now() + ",\n" +
            "  \"map\": \"" + AMap.getMapName() + "\",\n" +
            "  \"p0_name\": \"" + playerUs.name() + "\",\n" +
            "  \"p0_race\": \"" + playerUs.getRace().name() + "\",\n" +
            "  \"p0_win\": " + (AGame.won() ? "true" : "false") + ",\n" +
            "  \"p1_name\": \"" + playerEnemy.name() + "\",\n" +
            "  \"p1_race\": \"" + playerEnemy.getRace().name() + "\",\n" +
            "  \"p1_win\": " + (AGame.lost() ? "true" : "false") + "\n" +
            "}";

        String cherryVisDirPath = config.cherryVisDirReplayPath();

        A.saveToFile(
            cherryVisDirPath + "\\game_summary.json",
            content,
            true
        );
    }
}
