package benchmark;

import atlantis.Atlantis;
import atlantis.game.AGame;
import atlantis.game.util.AGameScore;
import atlantis.map.AMap;

public class BenchmarkResult {
    public String map;
    private boolean winner;
    public int score;
    public int killed;
    public int lost;
    public int resourcesBalance;
    public String time;

    public BenchmarkResult(boolean winner) {
        int score = AGameScore.scoreCurrentGame(winner);
        int killed = Atlantis.KILLED;
        int lost = Atlantis.LOST;
        int resourcesBalance = AGame.killsLossesResourceBalance();
        String time = AGame.timeSeconds() + "";

        this.map = AMap.mapFileNameWithoutPath();
        this.winner = winner;
        this.score = score;
        this.killed = killed;
        this.lost = lost;
        this.resourcesBalance = resourcesBalance;
        this.time = time;
    }

    public String toCsv() {
        return String.format(
            "%s,%s,%d,%d,%d,%d,%s",
            map,
            winner ? "v" : "x",
            score, killed, lost, resourcesBalance, time
        );
    }
}

