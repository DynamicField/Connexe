package fr.connexe.ui;

import java.io.File;
import java.util.prefs.Preferences;

/// General settings for the app to remember across sessions
public class Settings {
    private static final Preferences prefs = Preferences.userNodeForPackage(Settings.class);

    // Last directory that was opened in a FileChooser so the user gets better QoL when opening/saving files
    private static final String LAST_DIR_KEY = "lastVisitedDirectory";

    private Settings() {
        // Static only!
    }

    /// Retrieve last visited directory saved in the Preferences
    /// @return the last visited directory, or null if it doesn't exist
    public static File getLastVisitedDirectory() {
        String path = prefs.get(LAST_DIR_KEY, null);
        return (path != null) ? new File(path) : null;
    }

    /// Register last visited directory into the Preferences
    /// @param dir the directory to register
    public static void setLastVisitedDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            prefs.put(LAST_DIR_KEY, dir.getAbsolutePath());
        }
    }

}
