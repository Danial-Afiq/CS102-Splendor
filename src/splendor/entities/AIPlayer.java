package splendor.entities;

import splendor.logic.GameState;
import splendor.logic.ai.AIAction;
import splendor.logic.ai.AIStrategy;

/**
 * Represents a player controlled by an {@link splendor.logic.ai.AIStrategy}.
 */
public class AIPlayer extends Player {

    private final AIStrategy strategy;

    public AIPlayer(String name, AIStrategy strategy) {
        super(name);
        this.strategy = strategy;
    }

    public AIAction chooseAction(GameState state) {
        return strategy.selectAction(state, this);
    }

    public GemColor chooseGemToDiscard(GameState state) {
        return strategy.chooseGemToDiscard(state, this);
    }

    public static boolean isAI(Player player) {
        return player instanceof AIPlayer;
    }
}
