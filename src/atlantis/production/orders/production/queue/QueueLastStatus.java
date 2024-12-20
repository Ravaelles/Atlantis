package atlantis.production.orders.production.queue;

public class QueueLastStatus {
    private static String status = "-";
    private static String what = "-";

    public static void updateStatusOk(String what) {
        status = "OK";
        QueueLastStatus.what = what;
    }

    public static void updateStatusFailed(String why, String what) {
        QueueLastStatus.status = why;
        QueueLastStatus.what = what;
    }

    public static String status() {
        return status + " (" + what + ")";
    }
}
