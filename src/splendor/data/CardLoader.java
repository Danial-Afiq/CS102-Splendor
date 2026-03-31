package splendor.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import splendor.entities.Card;
import splendor.entities.CardDeck;
import splendor.entities.GemColor;
import splendor.entities.Tier;

/**
 * Loads development cards from CSV data.
 */
public class CardLoader {
    private static final int EXPECTED_COLUMNS = 8;
    private static final String VALID_BONUS_COLORS = "DIAMOND, SAPPHIRE, EMERALD, RUBY, ONYX";

    /**
     * Loads one deck per tier from a CSV file.
     */
    public static Map<Tier, CardDeck> loadDecks(String filePath) throws IOException {
        List<Card> tierOneCards = new ArrayList<Card>();
        List<Card> tierTwoCards = new ArrayList<Card>();
        List<Card> tierThreeCards = new ArrayList<Card>();

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

                Tier tier = parseTier(parts[0], filePath, lineNumber);
                int points = parseNonNegativeInt(parts[1], "points", filePath, lineNumber);
                GemColor bonus = parseBonusColor(parts[2], filePath, lineNumber);

                EnumMap<GemColor, Integer> cost = new EnumMap<GemColor, Integer>(GemColor.class);
                cost.put(GemColor.DIAMOND, parseNonNegativeInt(parts[3], "diamond cost", filePath, lineNumber));
                cost.put(GemColor.SAPPHIRE, parseNonNegativeInt(parts[4], "sapphire cost", filePath, lineNumber));
                cost.put(GemColor.EMERALD, parseNonNegativeInt(parts[5], "emerald cost", filePath, lineNumber));
                cost.put(GemColor.RUBY, parseNonNegativeInt(parts[6], "ruby cost", filePath, lineNumber));
                cost.put(GemColor.ONYX, parseNonNegativeInt(parts[7], "onyx cost", filePath, lineNumber));
                cost.put(GemColor.GOLD, 0);

                Card card = new Card(tier, points, bonus, cost);

                if (tier == Tier.ONE) {
                    tierOneCards.add(card);
                } else if (tier == Tier.TWO) {
                    tierTwoCards.add(card);
                } else {
                    tierThreeCards.add(card);
                }
            }
        }

        CardDeck tierOneDeck = new CardDeck(Tier.ONE, tierOneCards);
        CardDeck tierTwoDeck = new CardDeck(Tier.TWO, tierTwoCards);
        CardDeck tierThreeDeck = new CardDeck(Tier.THREE, tierThreeCards);

        Map<Tier, CardDeck> decks = new HashMap<Tier, CardDeck>();
        decks.put(Tier.ONE, tierOneDeck);
        decks.put(Tier.TWO, tierTwoDeck);
        decks.put(Tier.THREE, tierThreeDeck);

        return decks;
    }

    private static void validateColumnCount(String filePath, int lineNumber, int actualColumns)
            throws IOException {
        if (actualColumns != EXPECTED_COLUMNS) {
            throw invalidData(filePath, lineNumber,
                    "expected " + EXPECTED_COLUMNS + " columns but found " + actualColumns + ".");
        }
    }

    private static Tier parseTier(String rawTier, String filePath, int lineNumber) throws IOException {
        int tierNumber = parseNonNegativeInt(rawTier, "tier", filePath, lineNumber);
        if (tierNumber == 1) {
            return Tier.ONE;
        }
        if (tierNumber == 2) {
            return Tier.TWO;
        }
        if (tierNumber == 3) {
            return Tier.THREE;
        }
        throw invalidData(filePath, lineNumber,
                "invalid tier '" + rawTier.trim() + "' (expected 1, 2, or 3).");
    }

    private static GemColor parseBonusColor(String rawBonus, String filePath, int lineNumber)
            throws IOException {
        String normalized = rawBonus.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw invalidData(filePath, lineNumber,
                    "bonus color is blank (expected one of: " + VALID_BONUS_COLORS + ").");
        }

        try {
            GemColor bonus = GemColor.valueOf(normalized);
            if (bonus == GemColor.GOLD) {
                throw invalidData(filePath, lineNumber,
                        "invalid bonus color '" + rawBonus.trim() + "' (expected one of: "
                                + VALID_BONUS_COLORS + ").");
            }
            return bonus;
        } catch (IllegalArgumentException e) {
            throw invalidData(filePath, lineNumber,
                    "invalid bonus color '" + rawBonus.trim() + "' (expected one of: "
                            + VALID_BONUS_COLORS + ").");
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
                "Invalid card data in '" + filePath + "' at line " + lineNumber + ": " + message);
    }
}
