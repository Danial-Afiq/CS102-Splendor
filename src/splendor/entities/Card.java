package splendor.entities;

import java.util.EnumMap;

/**
 * Represents a development card.
 */
public class Card {
    private final Tier tier;
    private final int points;
    private final GemColor bonus;
    private final EnumMap<GemColor, Integer> cost;

    public Card(Tier tier, int points, GemColor bonus, EnumMap<GemColor, Integer> cost) {
        this.tier = tier;
        this.points = points;
        this.bonus = bonus;
        this.cost = cost;
    }

    public Tier getTier() {
        return tier;
    }

    public int getPoints() {
        return points;
    }

    public GemColor getBonus() {
        return bonus;
    }

    public EnumMap<GemColor, Integer> getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.format("Card{Tier=%s, Points=%s, Bonus=%s, Cost=%s}", 
                                tier, points, bonus, cost);
    }
}
