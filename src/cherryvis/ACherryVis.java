package cherryvis;

import atlantis.config.env.Env;
import atlantis.game.A;
import cherryvis.simple.ASimpleCherryVisLogger;

public class ACherryVis {
    public static boolean isEnabled() {
        return Env.generateCherryVisReplay();
    }

    // =========================================================

    private static ACherryVisLogger instance = null;

//    public static ACherryVisLogger instance() {
//        return instance;
//    }

    public static void initialize() {
//        String cherryVisFile = pathForCherryVisFiles();
//        if (A.fileExists(cherryVisFile)) {
//            A.removeFile(cherryVisFile);
//        }

//        CherryVis.initialize(cherryVisFile);
        // In your main bot class, when the game begins:
//        CherryVis.getInstance().initialize();

        logger();
    }

    public static ACherryVisLogger logger() {
        if (instance == null) {
            instance = new ASimpleCherryVisLogger(config());
        }

        return instance;
    }

    private static ACherryVisConfig config() {
        return ACherryVisConfig.createDefault();
    }

    public static void update() {
        logger().onFrameStart(A.now());
    }

    public static void finish() {
        System.out.println("CherryVis finishing...");
        logger().onGameEnd();

//        String pathForCherryVisFiles = pathForCherryVisFiles();
//        if (A.fileExists(pathForCherryVisFiles)) {
//            A.moveFile(pathForCherryVisFiles, Env.copyCherryVisDataTo() + "\\" + cherryVisFile());
//        }
    }

    // =========================================================

//    private static String pathForCherryVisFiles() {
////        return System.getProperty("user.dir") + "\\bwapi-data\\write\\" + cherryVisFile();
//        return System.getProperty("user.dir") + "\\bwapi-data\\write\\" + cherryVisFile();
//    }

//    private static String cherryVisFile() {
//        return "cherryvis.txt";
//    }
}
