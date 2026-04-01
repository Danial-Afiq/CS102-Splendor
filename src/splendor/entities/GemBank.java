package splendor.entities;

import java.util.EnumMap;

/**
 * Tracks the shared supply of gem tokens.
 */
public class GemBank {
    private static final int DEFAULT_TWO_PLAYER_GEM_COUNT = 4;
    private static final int DEFAULT_THREE_PLAYER_GEM_COUNT = 5;
    private static final int DEFAULT_FOUR_PLAYER_GEM_COUNT = 7;
    private static final int DEFAULT_GOLD_COUNT = 5;

    private final EnumMap<GemColor, Integer> gems;

    /**
     * Creates a bank with Splendor's token counts for 2 to 4 players.
     *
     * @throws IllegalArgumentException if {@code numPlayers} is not 2, 3, or 4
     */
    public GemBank(int numPlayers) {
        this(numPlayers, getDefaultStandardGemCount(numPlayers));
    }

    /**
     * Creates a bank with a custom standard token count for the five non-gold gem colors.
     *
     * @throws IllegalArgumentException if {@code numPlayers} is not 2, 3, or 4
     * @throws IllegalArgumentException if {@code standardGemCount} is not positive
     */
    public GemBank(int numPlayers, int standardGemCount) {
        this.gems = new EnumMap<>(GemColor.class);

        getDefaultStandardGemCount(numPlayers);
        if (standardGemCount <= 0) {
            throw new IllegalArgumentException("Standard gem count must be positive.");
        }

        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                gems.put(color, DEFAULT_GOLD_COUNT);
            } else {
                gems.put(color, standardGemCount);
            }
        }
    }

    public static int getDefaultStandardGemCount(int numPlayers) {
        if (numPlayers == 2) {
            return DEFAULT_TWO_PLAYER_GEM_COUNT;
        }
        if (numPlayers == 3) {
            return DEFAULT_THREE_PLAYER_GEM_COUNT;
        }
        if (numPlayers == 4) {
            return DEFAULT_FOUR_PLAYER_GEM_COUNT;
        }
        throw new IllegalArgumentException("Number of players must be 2, 3, or 4.");
    }

    public int getGemCount(GemColor color) {
        Integer count = gems.get(color);
        return count == null ? 0 : count;
    }

    public EnumMap<GemColor, Integer> getGems() {
        return new EnumMap<GemColor, Integer>(gems);
    }

    public boolean hasAtLeast(GemColor color, int amount) {
        return getGemCount(color) >= amount;
    }

    public boolean takeGem(GemColor color) {
        if (!hasAtLeast(color, 1)) {
            return false;
        }
        gems.put(color, getGemCount(color) - 1);
        return true;
    }

    public void addGem(GemColor color) {
        gems.put(color, getGemCount(color) + 1);
    }

    @Override
    public String toString() {
        return "GemBank{" +
                "gems=" + gems +
                "}";
    }
}
