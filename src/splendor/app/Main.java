package splendor.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import splendor.entities.GemColor;
import splendor.entities.Tier;
import splendor.logic.GameEngine;
import splendor.logic.GameSetup;
import splendor.logic.GameState;

public class Main {
    public static void main(String[] args) {
        try {
            List<String> playerNames = Arrays.asList("Alice", "Bob", "Charlie");
            GameState gameState = GameSetup.createGame(
                    playerNames,
                    "src/splendor/data/cards.csv",
                    "src/splendor/data/nobles.csv",
                    "config.properties");
            GameEngine engine = new GameEngine(gameState);

            System.out.println("=== Initial Game State ===");
            printState(gameState);

            System.out.println("=== Turn 1: " + engine.getCurrentPlayer().getName() + " ===");
            System.out.println("Take 3 different gems: " +
                    engine.takeThreeDifferentGems(GemColor.DIAMOND, GemColor.SAPPHIRE, GemColor.EMERALD));
            printState(gameState);
            engine.nextTurn();

            System.out.println("=== Turn 2: " + engine.getCurrentPlayer().getName() + " ===");
            System.out.println("Take 2 ruby gems: " + engine.takeTwoSameGems(GemColor.RUBY));
            printState(gameState);
            engine.nextTurn();

            System.out.println("=== Turn 3: " + engine.getCurrentPlayer().getName() + " ===");
            System.out.println("Reserve first tier 1 card: " + engine.reserveVisibleCard(Tier.ONE, 0));
            printState(gameState);
            engine.nextTurn();

            System.out.println("=== Turn 4: " + engine.getCurrentPlayer().getName() + " ===");
            System.out.println("Buy first tier 1 card if possible: " + engine.buyVisibleCard(Tier.ONE, 0));
            printState(gameState);
            System.out.println("Game over: " + engine.isGameOver());
        } catch (IOException e) {
            System.out.println("Error setting up game: " + e.getMessage());
        }
    }

    private static void printState(GameState gameState) {
        System.out.println("Current player: " + gameState.getCurrentPlayer().getName());
        System.out.println("Gem bank: " + gameState.getGemBank());
        System.out.println("Nobles in play: " + gameState.getNoblesInPlay());
        System.out.println("Visible tier 1 cards: " + gameState.getVisibleCards(Tier.ONE));
        System.out.println("Visible tier 2 cards: " + gameState.getVisibleCards(Tier.TWO));
        System.out.println("Visible tier 3 cards: " + gameState.getVisibleCards(Tier.THREE));
        System.out.println("Players:");
        for (int i = 0; i < gameState.getPlayers().size(); i++) {
            System.out.println("  " + (i + 1) + ". " + gameState.getPlayers().get(i));
        }
        System.out.println();
    }
}
