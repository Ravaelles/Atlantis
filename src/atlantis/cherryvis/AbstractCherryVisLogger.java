package atlantis.cherryvis;

import atlantis.units.AUnit;

public interface AbstractCherryVisLogger {
    void onFrameStart(int frame);
    void onGameEnd();
    void log(String message);
    void unitManager(String message, AUnit unit);
    void unitTooltip(String tooltip, AUnit unit);
    ACherryVisConfig config();
}
