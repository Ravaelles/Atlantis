package atlantis.cherryvis.simple;

import atlantis.game.A;

import java.util.ArrayList;
import java.util.List;

public class ACherryVis_GlobalLog {
    // {
    // "frame": currentFrame,
    // "message": "Creating new squad with goal AttackEnemyBase",
    // "sev": 0,
    // "file": "SquadManager.java",
    // "line": 24,
    // "attachments": []
    // }

    public static List<ACherryVis_GlobalLog> all = new ArrayList<>();

    private int frame;
    private String message;
    private int sev = 0;
    private String file = "";
    private int line = 0;

    private ACherryVis_GlobalLog(String message, String file) {
        this.frame = A.now;
        this.message = message;
        this.file = file;

        all.add(this);
    }

    public static ACherryVis_GlobalLog create(String message, String file) {
        return new ACherryVis_GlobalLog(message, file);
    }

    public String toJson() {
        String safeMessage = message != null ? message.replace("\\", "\\\\").replace("\"", "\\\"") : "";
        String safeFile = file != null ? file.replace("\\", "\\\\").replace("\"", "\\\"") : "";

        return "{"
            + "\"frame\":" + frame + ","
            + "\"message\":\"" + safeMessage + "\","
            + "\"sev\":" + sev + ","
            + "\"file\":\"" + safeFile + "\","
            + "\"line\":" + line + ","
            + "\"attachments\":[]"
            + "}";
    }
}
