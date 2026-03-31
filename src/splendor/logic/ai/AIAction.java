package splendor.logic.ai;

import java.util.List;
import splendor.entities.GemColor;
import splendor.entities.Tier;

/**
 * Represents a fully specified action selected by an AI strategy.
 */
public record AIAction(
        ActionType type,

        // Used by TAKE_GEMS
        List<GemColor> gems,

        // Used by BUY_CARD and RESERVE_CARD
        Tier tier,
        int slotIndex,

        // Used by BUY_CARD when buying from reserved hand
        boolean buyingReserved,
        int reservedIndex
) {

    /**
     * Enumerates AI action categories.
     */
    public enum ActionType {
        TAKE_GEMS,
        BUY_CARD,
        RESERVE_CARD
    }

    public static AIAction takeGems(List<GemColor> gems) {
        return new AIAction(ActionType.TAKE_GEMS, List.copyOf(gems),
                null, -1, false, -1);
    }

    public static AIAction buyVisible(Tier tier, int slotIndex) {
        return new AIAction(ActionType.BUY_CARD, List.of(),
                tier, slotIndex, false, -1);
    }

    public static AIAction buyReserved(int reservedIndex) {
        return new AIAction(ActionType.BUY_CARD, List.of(),
                null, -1, true, reservedIndex);
    }

    public static AIAction reserveVisible(Tier tier, int slotIndex) {
        return new AIAction(ActionType.RESERVE_CARD, List.of(),
                tier, slotIndex, false, -1);
    }

    public static AIAction reserveTopOfDeck(Tier tier) {
        return new AIAction(ActionType.RESERVE_CARD, List.of(),
                tier, -1, false, -1);
    }

    @Override
    public String toString() {
        return switch (type) {
            case TAKE_GEMS     -> "Take gems: " + gems;
            case BUY_CARD      -> buyingReserved
                    ? "Buy reserved card #" + (reservedIndex + 1)
                    : "Buy card [tier=" + tier + ", slot=" + (slotIndex + 1) + "]";
            case RESERVE_CARD  -> slotIndex == -1
                    ? "Reserve top of deck [tier=" + tier + "]"
                    : "Reserve card [tier=" + tier + ", slot=" + (slotIndex + 1) + "]";
        };
    }
}
