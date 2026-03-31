package splendor.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a development-card deck for one tier.
 */
public class CardDeck {

    private final Tier tier;
    private final List<Card> cards; 

    public CardDeck(Tier tier, List<Card> cards) {
        if (tier == null) { throw new IllegalArgumentException("tier cannot be null"); } 
        if (cards == null) { throw new IllegalArgumentException("cards cannot be null"); } 
        this.tier = tier; 
        this.cards = new ArrayList<Card>(cards);
    } 
    
    public Tier getTier() { 
        return tier; 
    } 

    public int size() { 
        return cards.size(); 
    } 

    public boolean isEmpty() { 
        return cards.isEmpty(); 
    } 

    public void shuffle() {
        Collections.shuffle(cards); 
    } 

    /**
     * Draws the top card, or returns {@code null} when empty.
     */
    public Card drawCard() {
        if (cards.isEmpty()) { 
            return null; 
        } 
        return cards.remove(0); 
    } 

    /**
     * Peeks at the top card, or returns {@code null} when empty.
     */
    public Card peekTopCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.get(0);
    }

    @Override 
    public String toString() {
        return "CardDeck{" + "tier=" + tier + ", size=" + cards.size() + "}"; 
    } 
}
