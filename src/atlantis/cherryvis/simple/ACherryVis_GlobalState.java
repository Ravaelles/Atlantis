package atlantis.cherryvis.simple;

import atlantis.game.A;

import java.util.ArrayList;
import java.util.List;

public class ACherryVis_GlobalState {
    public static List<ACherryVis_GlobalState> all = new ArrayList<>();

    private int frame;
    private String key;
    private String value;

    private ACherryVis_GlobalState(String key, String value) {
        this.frame = A.now;
        this.key = key;
        this.value = value;

        all.add(this);
    }

    public static ACherryVis_GlobalState create(String message, String file) {
        return new ACherryVis_GlobalState(message, file);
    }

    public String toJson() {
        return "\"" + frame + "\": {"
            + "\"" + key + "\":\"" + value + "\""
            + "}";
    }
}
