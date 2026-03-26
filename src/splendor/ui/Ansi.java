package splendor.ui;

import splendor.entities.GemColor;

public final class Ansi {
    public static final boolean ENABLED = System.getenv("NO_COLOR") == null;

    public static final String RESET = code("0");
    public static final String BOLD = code("1");
    public static final String DIM = code("2");
    public static final String CYAN = code("38;5;117");
    public static final String RED = code("38;5;210");
    public static final String BLUE = code("38;5;117");
    public static final String GREEN = code("38;5;120");
    public static final String YELLOW = code("38;5;221");
    public static final String WHITE = code("38;5;255");
    public static final String PURPLE = code("38;5;177");
    public static final String BROWN = code("38;5;173");
    public static final String GOLD = code("38;5;220");
    public static final String GRAY = code("38;5;245");
    public static final String CLEAR_SCREEN = ENABLED ? "\u001B[2J" : "";
    public static final String CURSOR_HOME = ENABLED ? "\u001B[H" : "";

    private Ansi() {
    }

    private static String code(String value) {
        if (!ENABLED) {
            return "";
        }
        return "\u001B[" + value + "m";
    }

    public static String wrap(String colorCode, String text) {
        if (!ENABLED) {
            return text;
        }
        return colorCode + text + RESET;
    }

    public static String style(String text, String... styles) {
        if (!ENABLED) {
            return text;
        }

        StringBuilder builder = new StringBuilder();
        for (String style : styles) {
            builder.append(style);
        }
        builder.append(text).append(RESET);
        return builder.toString();
    }

    public static String accent(String text) {
        return wrap(CYAN, text);
    }

    public static String success(String text) {
        return wrap(GREEN, text);
    }

    public static String error(String text) {
        return wrap(RED, text);
    }

    public static String dim(String text) {
        return wrap(DIM + GRAY, text);
    }

    public static String colorForGem(GemColor color) {
        if (color == GemColor.DIAMOND) {
            return WHITE;
        }
        if (color == GemColor.SAPPHIRE) {
            return BLUE;
        }
        if (color == GemColor.EMERALD) {
            return GREEN;
        }
        if (color == GemColor.RUBY) {
            return RED;
        }
        if (color == GemColor.ONYX) {
            return BROWN;
        }
        return GOLD;
    }
}
