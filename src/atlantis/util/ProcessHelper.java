package atlantis.util;

/**
 * Kills and starts processes, including Starcraft game itself and Chaoslauncher.
 */
public class ProcessHelper {

    public static void killStarcraftProcess() {
        executeInCommandLine("taskkill /f /im Starcraft.exe");
        executeInCommandLine("taskkill /f /im StarCraft.exe");
    }
    
    public static void killChaosLauncherProcess() {
        executeInCommandLine("taskkill /f /im Chaoslauncher.exe");
    }
    
    /**
     * Autostart Chaoslauncher
     * Combined with Chaoslauncher -> Settings -> Run Starcraft on Startup 
     * SC will be autostarted at this moment
     */
    public static void startChaosLauncherProcess() {
        try {
            Thread.sleep(250);
            executeInCommandLine("C:\\Program Files (x86)\\BWAPI\\Chaoslauncher\\Chaoslauncher.exe");
        } catch (InterruptedException ex) {
            // Don't do anything
        }
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
