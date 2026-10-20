package starengine;

import starengine.assets.Images;
import starengine.assets.Map;
import starengine.engine_game.StarEngineGame;
import starengine.events.EngineUpdater;
import starengine.units.Units;
import starengine.units.UnitsFromFakes;
import tests.acceptance.AbstractWorldCreatingTest;

import javax.swing.*;

public class StarEngine {
    private final AbstractWorldCreatingTest testClass;
    public Map map;
    public Units units;
    private StarEngineGame engineGame;
    private JFrame window;

    // =========================================================

    public StarEngine(AbstractWorldCreatingTest test) {
        this.testClass = test;
        Images.loadAllImages();

        map = new Map();
        map.init();

        units = new UnitsFromFakes(this, map);

        engineGame = new StarEngineGame(this);
    }

    // =========================================================

    public void updateOnFrameEnd() {
        EngineUpdater.update(this);
    }

    public void closeIfNeeded() {
        if (StarEngineConfig.AUTO_CLOSE_WINDOW_ON_GAME_END) close();
    }

    public void close() {
        if (window != null) window.dispose();
    }

    // =========================================================

    public StarEngineGame game() {
        return engineGame;
    }

    public AbstractWorldCreatingTest testClass() {
        return testClass;
    }

    public void setWindow(JFrame window) {
        this.window = window;
    }
}
