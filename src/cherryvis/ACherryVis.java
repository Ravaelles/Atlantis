package cherryvis;

import atlantis.config.env.Env;
import atlantis.game.A;
import cherryvis.java.CherryVis;

public class ACherryVis {
    public static boolean isEnabled() {
        if (true) return false;
        return Env.isLocal();
    }

    // =========================================================

    public static void initialize() {
        String cherryVisFile = pathForCherryVisFiles();
        if (A.fileExists(cherryVisFile)) {
            A.removeFile(cherryVisFile);
        }

//        CherryVis.initialize(cherryVisFile);
        // In your main bot class, when the game begins:
        CherryVis.getInstance().initialize();
    }

    public static void update() {
        CherryVis.getInstance().onFrameStart(A.now());
    }

    public static void finish() {
        CherryVis.getInstance().onGameEnd();

        String pathForCherryVisFiles = pathForCherryVisFiles();
        if (A.fileExists(pathForCherryVisFiles)) {
            A.moveFile(pathForCherryVisFiles, Env.copyCherryVisDataTo() + "\\" + cherryVisFile());
        }
    }

    // =========================================================

    private static String pathForCherryVisFiles() {
        return System.getProperty("user.dir") + "\\bwapi-data\\write\\" + cherryVisFile();
    }

    private static String cherryVisFile() {
        return "cherryvis.txt";
    }
}
