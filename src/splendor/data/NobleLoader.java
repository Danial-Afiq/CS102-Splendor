package splendor.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import splendor.entities.GemColor;
import splendor.entities.Noble;

/**
 * Loads nobles from CSV data.
 */
public class NobleLoader {
    private static final int EXPECTED_COLUMNS = 7;

    /**
     * Loads nobles from a CSV file.
     */
    public static List<Noble> loadNobles(String filePath) throws IOException {
        List<Noble> nobles = new ArrayList<Noble>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(",", -1);
                validateColumnCount(filePath, lineNumber, parts.length);

                String id = parts[0].trim();
                if (id.isEmpty()) {
                    throw invalidData(filePath, lineNumber, "noble id cannot be blank.");
                }

                int points = parseNonNegativeInt(parts[1], "points", filePath, lineNumber);

                EnumMap<GemColor, Integer> requirements = new EnumMap<GemColor, Integer>(GemColor.class);
                requirements.put(GemColor.DIAMOND,
                        parseNonNegativeInt(parts[2], "diamond requirement", filePath, lineNumber));
                requirements.put(GemColor.SAPPHIRE,
                        parseNonNegativeInt(parts[3], "sapphire requirement", filePath, lineNumber));
                requirements.put(GemColor.EMERALD,
                        parseNonNegativeInt(parts[4], "emerald requirement", filePath, lineNumber));
                requirements.put(GemColor.RUBY,
                        parseNonNegativeInt(parts[5], "ruby requirement", filePath, lineNumber));
                requirements.put(GemColor.ONYX,
                        parseNonNegativeInt(parts[6], "onyx requirement", filePath, lineNumber));
                requirements.put(GemColor.GOLD, 0);

                nobles.add(new Noble(id, points, requirements));
            }
        }

        return nobles;
    }

    private static void validateColumnCount(String filePath, int lineNumber, int actualColumns)
            throws IOException {
        if (actualColumns != EXPECTED_COLUMNS) {
            throw invalidData(filePath, lineNumber,
                    "expected " + EXPECTED_COLUMNS + " columns but found " + actualColumns + ".");
        }
    }

    private static int parseNonNegativeInt(String rawValue, String fieldName, String filePath, int lineNumber)
            throws IOException {
        String trimmed = rawValue.trim();
        try {
            int value = Integer.parseInt(trimmed);
            if (value < 0) {
                throw invalidData(filePath, lineNumber,
                        fieldName + " cannot be negative (found '" + trimmed + "').");
            }
            return value;
        } catch (NumberFormatException e) {
            throw invalidData(filePath, lineNumber,
                    "invalid " + fieldName + " '" + trimmed + "' (expected a non-negative integer).");
        }
    }

    private static IOException invalidData(String filePath, int lineNumber, String message) {
        return new IOException(
                "Invalid noble data in '" + filePath + "' at line " + lineNumber + ": " + message);
    }
}
