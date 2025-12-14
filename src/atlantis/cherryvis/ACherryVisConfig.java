package atlantis.cherryvis;

import atlantis.game.A;

public class ACherryVisConfig {
    private String cherryVisReplayDirectory = null;

    // =========================================================

    public static ACherryVisConfig createDefault() {
        return new ACherryVisConfig();
    }

    // =========================================================

    public String cherryVisDirReplayPath() {
        if (cherryVisReplayDirectory != null) {
            return cherryVisReplayDirectory;
        }

        ACherryVisConfig config = ACherryVis.logger().config();
//        String basePath = Env.copyCherryVisDataTo();
        String basePath = "bwapi-data/write";
        cherryVisReplayDirectory = basePath + "/" + config.useDirectoryName();

        if (!A.directoryExists(cherryVisReplayDirectory)) A.createDirectory(cherryVisReplayDirectory);

        return cherryVisReplayDirectory;
    }

    private String useDirectoryName() {
        return "cherryvis_dir";
//        + ".rep.cvis"
//        return "_" + A.hourMin() + "_" + Enemy.name();
    }
}
