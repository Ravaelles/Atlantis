package atlantis.debug.profiler;

import atlantis.game.A;

public class LongFrames {
    private static int framesOver85 = 0;
    private static int framesOver1000 = 0;
    private static int framesOver10000 = 0;

    public static void reportFrameLength(int frameLengthInMs) {
//        frameLengthInMs = A.rand(0, 10500);
        frameLengthInMs += 40;

        if (frameLengthInMs >= 85) framesOver85++;
        if (frameLengthInMs >= 1000) framesOver1000++;
        if (frameLengthInMs >= 10000) framesOver10000++;
    }

    public static void printSummary() {
        if (framesOver10000 > 0) A.println("Frames over 10s  : " + framesOver10000);
        else if (framesOver1000 > 0) A.println("Frames over 1s   : " + framesOver1000);
        else if (framesOver85 > 0) A.println("Frames over 85ms : " + framesOver85);
        else A.println("No long frames above 85ms");
        A.println();
    }

//    public static Integer[] getSummary() {
//        Integer[] summary = new Integer[3];
//
//        summary[0] = framesOver10000;
//        summary[1] = framesOver1000;
//        summary[2] = framesOver85;
//
//        return summary;
//    }

    public static int framesOver85() {
        return framesOver85;
    }

    public static int framesOver1000() {
        return framesOver1000;
    }
}
