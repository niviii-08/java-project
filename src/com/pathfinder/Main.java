package com.pathfinder;

import com.pathfinder.ui.SplashScreen;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Auto-initialize DB on startup
            com.pathfinder.db.DbInit.main(args);
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.setVisible(true);
        });
    }
}
