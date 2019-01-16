package atlantis.util;

/**
 * Kills and starts processes, including Starcraft game itself and Chaoslauncher.
 */
public class ProcessHelper {

    public static void killStarcraftProcess() {
        executeInCommandLine("taskkill /IM starcraft.exe /T /F");
        executeInCommandLine("taskkill /IM Starcraft.exe /T /F");
        executeInCommandLine("taskkill /IM StarCraft.exe /T /F");
    }
    
    public static void killChaosLauncherProcess() {
        executeInCommandLine("taskkill /IM chaoslauncher.exe /T /F");
        executeInCommandLine("taskkill /IM Chaoslauncher.exe /T /F");
    }
    
    /**
     * Autostart Chaoslauncher
     * Combined with Chaoslauncher -> Settings -> Run Starcraft on Startup 
     * SC will be autostarted at this moment
     */
    public static void startChaosLauncherProcess() {
        try {
            Thread.sleep(250);
            executeInCommandLine("cmd /c D:\\GAMES\\BWAPI\\Chaoslauncher\\Chaoslauncher.exe");
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
