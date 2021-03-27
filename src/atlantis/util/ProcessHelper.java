package atlantis.util;

/**
 * Kills and starts processes, including Starcraft game itself and Chaoslauncher.
 */
public class ProcessHelper {
    public static String ChaosLauncherPath = "D:\\JAVA\\StarCraftAI\\BWAPI\\Chaoslauncher\\Chaoslauncher.exe";

    public static void killStarcraftProcess() {
        executeInCommandLine("taskkill /IM StarCraft.exe /T /F");
    }
    
    public static void killChaosLauncherProcess() {
        executeInCommandLine("taskkill /IM Chaoslauncher.exe /T /F");
    }
    
    /**
     * Autostart Chaoslauncher
     * Combined with Chaoslauncher -> Settings -> Run Starcraft on Startup 
     * SC will be autostarted at this moment
     */
    public static void startChaosLauncherProcess() {
        try {
            Thread.sleep(150);
            String command = "cmd /c " + ProcessHelper.ChaosLauncherPath;

            executeInCommandLine(command);
        } catch (InterruptedException ignored) { }
    }
    
    // =========================================================
    
    private static void executeInCommandLine(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

}
