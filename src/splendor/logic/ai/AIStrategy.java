package splendor.logic.ai;

import splendor.entities.GemColor;
import splendor.entities.Player;
import splendor.logic.GameState;

/**
 * Defines the contract for AI turn selection and discard decisions.
 */
public interface AIStrategy {

    /**
     * Selects an action for the current turn.
     */
    AIAction selectAction(GameState state, Player self);

    /**
     * Chooses a gem to discard after exceeding the token limit.
     */
    GemColor chooseGemToDiscard(GameState state, Player self);
}
