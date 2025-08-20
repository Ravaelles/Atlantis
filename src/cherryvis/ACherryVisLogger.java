package cherryvis;

import atlantis.units.AUnit;

public interface ACherryVisLogger {
    void onFrameStart(int frame);
    void onGameEnd();
    void unitActiveManager(String message, AUnit unit);
    void unitTooltip(String tooltip, AUnit unit);
    ACherryVisConfig config();
}
