package cherryvis;

/**
 * Game-end summary for the trace directory: who played, on which map,
 * how long the game lasted, and who won. Plain data, filled by the bot
 * adapter (it owns the game objects) and rendered here.
 */
public class GameSummary {
    public String openingBuildOrder = "";
    public String finalBuildOrder = "";
    public boolean draw = false;
    public int gameDurationFrames = 0;
    public String map = "";
    public String playerName = "";
    public String playerRace = "";
    public boolean playerWin = false;
    public String enemyName = "";
    public String enemyRace = "";
    public boolean enemyWin = false;

    public String toJson() {
        return "{\n"
            + "  \"cp_final_bo\": \"" + Json.escape(finalBuildOrder) + "\",\n"
            + "  \"cp_opening_bo\": \"" + Json.escape(openingBuildOrder) + "\",\n"
            + "  \"draw\": " + draw + ",\n"
            + "  \"game_duration_frames\": " + gameDurationFrames + ",\n"
            + "  \"map\": \"" + Json.escape(map) + "\",\n"
            + "  \"p0_name\": \"" + Json.escape(playerName) + "\",\n"
            + "  \"p0_race\": \"" + Json.escape(playerRace) + "\",\n"
            + "  \"p0_win\": " + playerWin + ",\n"
            + "  \"p1_name\": \"" + Json.escape(enemyName) + "\",\n"
            + "  \"p1_race\": \"" + Json.escape(enemyRace) + "\",\n"
            + "  \"p1_win\": " + enemyWin + "\n"
            + "}";
    }
}
