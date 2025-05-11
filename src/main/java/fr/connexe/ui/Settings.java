package fr.connexe.ui;

import java.io.File;
import java.util.prefs.Preferences;

/// General settings for the app to remember across sessions
public class Settings {
    private static final Preferences prefs = Preferences.userNodeForPackage(Settings.class);

    // Last directory that was opened in a FileChooser so the user gets better QoL when opening/saving files
    private static final String LAST_DIR_KEY = "lastVisitedDirectory";

    public static File getLastVisitedDirectory() {
        String path = prefs.get(LAST_DIR_KEY, null);
        return (path != null) ? new File(path) : null;
    }

    public static void setLastVisitedDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            prefs.put(LAST_DIR_KEY, dir.getAbsolutePath());
        }
    }

}
