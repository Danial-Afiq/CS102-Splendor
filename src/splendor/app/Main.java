package splendor.app;

import splendor.ui.ConsoleGameUI;

/**
 * Launches the console-based Splendor application.
 */
public class Main {
    public static void main(String[] args) {
        ConsoleGameUI ui = new ConsoleGameUI();
        ui.run();
    }
}
