package atlantis.tweaker;

import atlantis.Atlantis;
import atlantis.env.Env;
import atlantis.util.A;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

public class ParamTweakerEvaluator {

    private static final String DELIMITER = ";";

    public static void updateOnEnd(boolean winner) {
        saveScoreToFile(winner);
    }

    // =========================================================

    private static void saveScoreToFile(boolean winner) {
        String file = "param_tweaker/" + dateString() + ".csv";

        if (Env.isFirstRun()) {
            A.removeFile(file);
        }

        A.writeToFileWithHeader(file, resultString(winner), fileHeader());
    }

    private static String[] fileHeader() {
        String[] base = {
                "Date",
                "W/L",
                "Seconds",
                "Killed",
                "Lost",
                "Score"
        };
        return Stream.concat(Arrays.stream(base), Arrays.stream(paramNames())).toArray(String[]::new);
    }

    private static String[] paramNames() {
        ArrayList<String> list = new ArrayList<>();
        ParamTweaker tweaker = ParamTweaker.get();
        for (Param param : tweaker.params()) {
            list.add(param.name());
        }

        String[] stringArray = list.toArray(new String[0]);
        return stringArray;
    }

    private static String paramsString() {
        StringBuilder string = new StringBuilder();

        ParamTweaker tweaker = ParamTweaker.get();
        for (Param param : tweaker.params()) {
            if (string.length() > 0) {
                string.append(DELIMITER);
            }
            string.append(param.getterValue());
        }

        return string.toString();
    }

    private static String resultString(boolean winner) {
        return timeString() + DELIMITER
                + (winner ? "W" : "L") + DELIMITER
                + A.seconds() + DELIMITER
                + Atlantis.KILLED + DELIMITER
                + Atlantis.LOST + DELIMITER
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
