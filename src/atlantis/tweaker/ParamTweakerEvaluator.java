package atlantis.tweaker;

import atlantis.Atlantis;
import atlantis.util.A;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ParamTweakerEvaluator {

    private static final String DELIMITER = ";";

    public static void updateOnEnd(boolean winner) {
        saveScoreToFile(winner);
    }

    // =========================================================

    private static void saveScoreToFile(boolean winner) {
        String file = "param_tweaker/" + dateString() + ".csv";
        A.writeToFileAppending(file, resultString(winner), fileHeaders());
    }

    private static String[] fileHeaders() {
        return new String[] {
                "Date",
                "W/L",
                "Seconds",
                "Score",
                "Params",
        };
    }

    private static String paramsString() {
        StringBuilder string = new StringBuilder();
        string.append("\"{\n");

        ParamTweaker tweaker = ParamTweaker.get();
        for (Param param : tweaker.params()) {
            string.append("    ")
                    .append(param.name())
                    .append(": ")
                    .append(param.getterValue())
                    .append(",\n");
        }

        string.append("}\"");
        return string.toString();
    }

    private static String resultString(boolean winner) {
        return timeString() + DELIMITER
                + (winner ? "W" : "L") + DELIMITER
                + A.seconds() + DELIMITER
                + score(winner) + DELIMITER
                + paramsString();
    }

    private static int score(boolean winner) {
        int score = winner ? 0 : -500;

        score += A.resourcesBalance();

        score += Atlantis.KILLED * 10;
        score -= Atlantis.LOST * 40;

        score -= A.seconds();

        return score;
    }

    private static String dateString() {
        Date date = new Date();
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private static String timeString() {
        Date date = new Date();
        return new SimpleDateFormat("HH:mm").format(date);
    }

}
