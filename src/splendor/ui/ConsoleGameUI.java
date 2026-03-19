package splendor.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import splendor.entities.Card;
import splendor.entities.GemColor;
import splendor.entities.Noble;
import splendor.entities.Player;
import splendor.entities.Tier;
import splendor.logic.GameEngine;
import splendor.logic.GameSetup;
import splendor.logic.GameState;

public class ConsoleGameUI {
    private static final int ACTION_TAKE_THREE_DIFFERENT = 1;
    private static final int ACTION_TAKE_TWO_SAME = 2;
    private static final int ACTION_RESERVE_VISIBLE = 3;
    private static final int ACTION_RESERVE_TOP = 4;
    private static final int ACTION_BUY_VISIBLE = 5;
    private static final int ACTION_BUY_RESERVED = 6;
    private static final int ACTION_QUIT = 7;

    private final Scanner scanner;
    private boolean quitRequested;

    public ConsoleGameUI() {
        this.scanner = new Scanner(System.in);
        this.quitRequested = false;
    }

    public void run() {
        try {
            GameState gameState = createGameFromInput();
            GameEngine engine = new GameEngine(gameState);

            System.out.println();
            System.out.println("Game started.");

            while (!engine.isGameOver() && !quitRequested) {
                runTurn(engine);
            }

            if (quitRequested) {
                System.out.println();
                System.out.println("Game ended by user.");
            } else {
                printFinalResults(gameState);
            }
        } catch (IOException e) {
            System.out.println("Failed to start game: " + e.getMessage());
        }
    }

    private GameState createGameFromInput() throws IOException {
        System.out.println("=== Splendor CLI ===");
        int numPlayers = readIntInRange("Number of players (2-4): ", 2, 4);

        List<String> playerNames = new ArrayList<String>();
        for (int i = 1; i <= numPlayers; i++) {
            String name = readNonEmptyLine("Enter name for player " + i + ": ");
            playerNames.add(name);
        }

        return GameSetup.createGame(
                playerNames,
                "src/splendor/data/cards.csv",
                "src/splendor/data/nobles.csv",
                "config.properties");
    }

    private void runTurn(GameEngine engine) {
        boolean turnFinished = false;

        while (!turnFinished && !engine.isGameOver() && !quitRequested) {
            printGameState(engine.getGameState());
            List<Integer> availableActions = printActionMenu(engine);

            int choice = readIntInRange("Choose action: ", 1, availableActions.size());
            int actionCode = availableActions.get(choice - 1);
            turnFinished = handleActionChoice(engine, actionCode);

            if (turnFinished) {
                if (quitRequested) {
                    return;
                }

                System.out.println("Action successful.");
                if (!engine.isGameOver()) {
                    engine.nextTurn();
                }
            }
        }
    }

    private boolean handleActionChoice(GameEngine engine, int choice) {
        try {
            if (choice == ACTION_TAKE_THREE_DIFFERENT) {
                return handleTakeThreeDifferentGems(engine);
            }
            if (choice == ACTION_TAKE_TWO_SAME) {
                return handleTakeTwoSameGems(engine);
            }
            if (choice == ACTION_RESERVE_VISIBLE) {
                return handleReserveVisibleCard(engine);
            }
            if (choice == ACTION_RESERVE_TOP) {
                return handleReserveTopCard(engine);
            }
            if (choice == ACTION_BUY_VISIBLE) {
                return handleBuyVisibleCard(engine);
            }
            if (choice == ACTION_BUY_RESERVED) {
                return handleBuyReservedCard(engine);
            }

            quitRequested = true;
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
            return false;
        }
    }

    private boolean handleTakeThreeDifferentGems(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getTotalGems() + 3 > 10) {
            System.out.println("You cannot take 3 gems because that would put you over the 10-token limit.");
            return false;
        }

        List<GemColor> firstOptions = getAvailableGemColors(engine, 1, new ArrayList<GemColor>());
        if (firstOptions.size() < 3) {
            System.out.println("You cannot take 3 different gems because fewer than 3 gem colors are available in the bank.");
            return false;
        }

        System.out.println("Pick 3 different gem colors.");
        GemColor first = readGemColorFromOptions(firstOptions, "First color: ");

        List<GemColor> secondExcluded = new ArrayList<GemColor>();
        secondExcluded.add(first);
        GemColor second = readGemColorFromOptions(
                getAvailableGemColors(engine, 1, secondExcluded),
                "Second color: ");

        List<GemColor> thirdExcluded = new ArrayList<GemColor>();
        thirdExcluded.add(first);
        thirdExcluded.add(second);
        GemColor third = readGemColorFromOptions(
                getAvailableGemColors(engine, 1, thirdExcluded),
                "Third color: ");

        return engine.takeThreeDifferentGems(first, second, third);
    }

    private boolean handleTakeTwoSameGems(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getTotalGems() + 2 > 10) {
            System.out.println("You cannot take 2 gems because that would put you over the 10-token limit.");
            return false;
        }

        List<GemColor> options = getAvailableGemColors(engine, 4, new ArrayList<GemColor>());
        if (options.isEmpty()) {
            System.out.println("You cannot take 2 of the same gem because no color has at least 4 tokens in the bank.");
            return false;
        }

        GemColor color = readGemColorFromOptions(options, "Choose color to take 2 of: ");
        return engine.takeTwoSameGems(color);
    }

    private boolean handleReserveVisibleCard(GameEngine engine) {
        if (!canReserveCard(engine)) {
            System.out.println("You cannot reserve a card because " + getReserveCardUnavailableReason(engine) + ".");
            return false;
        }

        List<Tier> tierOptions = getTiersWithVisibleCards(engine);
        if (tierOptions.isEmpty()) {
            System.out.println("You cannot reserve a visible card because there are no visible cards on the board.");
            return false;
        }

        Tier tier = readTierFromOptions(tierOptions, "Choose tier to reserve from: ");
        List<Card> visibleCards = engine.getGameState().getVisibleCards(tier);

        printCardsWithNumbers(visibleCards);
        int index = readIntInRange("Card number to reserve: ", 1, visibleCards.size());
        return engine.reserveVisibleCard(tier, index - 1);
    }

    private boolean handleReserveTopCard(GameEngine engine) {
        if (!canReserveCard(engine)) {
            System.out.println("You cannot reserve a card because " + getReserveCardUnavailableReason(engine) + ".");
            return false;
        }

        List<Tier> tierOptions = getTiersWithCardsInDeck(engine);
        if (tierOptions.isEmpty()) {
            System.out.println("You cannot reserve from the top of a deck because all decks are empty.");
            return false;
        }

        Tier tier = readTierFromOptions(tierOptions, "Choose tier to reserve from the top: ");
        return engine.reserveTopCard(tier);
    }

    private boolean handleBuyVisibleCard(GameEngine engine) {
        List<Tier> tierOptions = getTiersWithAffordableVisibleCards(engine);
        if (tierOptions.isEmpty()) {
            System.out.println("You cannot buy a visible card because you cannot afford any visible cards right now.");
            return false;
        }

        Tier tier = readTierFromOptions(tierOptions, "Choose tier to buy from: ");
        List<Integer> affordableIndices = getAffordableVisibleCardIndices(engine, tier);
        List<Card> affordableCards = new ArrayList<Card>();
        for (int index : affordableIndices) {
            affordableCards.add(engine.getGameState().getVisibleCards(tier).get(index));
        }

        printCardsWithNumbers(affordableCards);
        int option = readIntInRange("Card number to buy: ", 1, affordableCards.size());
        int actualIndex = affordableIndices.get(option - 1);
        return engine.buyVisibleCard(tier, actualIndex);
    }

    private boolean handleBuyReservedCard(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getReservedCards().isEmpty()) {
            System.out.println("You cannot buy a reserved card because you do not have any reserved cards.");
            return false;
        }

        List<Integer> affordableIndices = getAffordableReservedCardIndices(engine);
        if (affordableIndices.isEmpty()) {
            System.out.println("You cannot buy a reserved card because you cannot afford any of your reserved cards right now.");
            return false;
        }

        List<Card> affordableCards = new ArrayList<Card>();
        for (int index : affordableIndices) {
            affordableCards.add(player.getReservedCards().get(index));
        }

        printCardsWithNumbers(affordableCards);
        int option = readIntInRange("Reserved card number to buy: ", 1, affordableCards.size());
        int actualIndex = affordableIndices.get(option - 1);
        return engine.buyReservedCard(actualIndex);
    }

    private void printGameState(GameState gameState) {
        Player currentPlayer = gameState.getCurrentPlayer();

        System.out.println();
        System.out.println("==================================================");
        System.out.println("Current player: " + currentPlayer.getName());
        System.out.println("Target points: " + gameState.getWinPoints());
        System.out.println("Final round started: " + gameState.isFinalRoundStarted());
        System.out.println();
        printBank(gameState);
        printNobles(gameState);
        printVisibleCards(gameState, Tier.ONE);
        printVisibleCards(gameState, Tier.TWO);
        printVisibleCards(gameState, Tier.THREE);
        printPlayerSummary(currentPlayer, true);
        printOtherPlayers(gameState);
    }

    private void printBank(GameState gameState) {
        System.out.println("Bank:");
        for (GemColor color : GemColor.values()) {
            System.out.println("  " + color + ": " + gameState.getGemBank().getGemCount(color));
        }
        System.out.println();
    }

    private void printNobles(GameState gameState) {
        System.out.println("Nobles in play:");
        if (gameState.getNoblesInPlay().isEmpty()) {
            System.out.println("  None");
        } else {
            for (int i = 0; i < gameState.getNoblesInPlay().size(); i++) {
                Noble noble = gameState.getNoblesInPlay().get(i);
                System.out.println("  " + (i + 1) + ". " + formatNoble(noble));
            }
        }
        System.out.println();
    }

    private void printVisibleCards(GameState gameState, Tier tier) {
        List<Card> cards = gameState.getVisibleCards(tier);
        System.out.println("Tier " + getTierNumber(tier) + " visible cards:");
        if (cards.isEmpty()) {
            System.out.println("  None");
        } else {
            for (int i = 0; i < cards.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + formatCard(cards.get(i)));
            }
        }
        System.out.println("  Deck remaining: " + gameState.getDeck(tier).size());
        System.out.println();
    }

    private void printPlayerSummary(Player player, boolean currentPlayer) {
        if (currentPlayer) {
            System.out.println("Your status:");
        } else {
            System.out.println(player.getName() + ":");
        }
        System.out.println("  Points: " + player.getPoints());
        System.out.println("  Gems: " + player.getGems());
        System.out.println("  Bonuses: " + buildBonusSummary(player));
        System.out.println("  Reserved cards:");
        if (player.getReservedCards().isEmpty()) {
            System.out.println("    None");
        } else {
            for (int i = 0; i < player.getReservedCards().size(); i++) {
                System.out.println("    " + (i + 1) + ". " + formatCard(player.getReservedCards().get(i)));
            }
        }
        System.out.println("  Purchased cards: " + player.getPurchasedCards().size());
        System.out.println("  Nobles: " + player.getNobles());
        System.out.println();
    }

    private void printOtherPlayers(GameState gameState) {
        System.out.println("Other players:");
        boolean foundOtherPlayer = false;

        for (Player player : gameState.getPlayers()) {
            if (player == gameState.getCurrentPlayer()) {
                continue;
            }
            foundOtherPlayer = true;
            printPlayerSummary(player, false);
        }

        if (!foundOtherPlayer) {
            System.out.println("  None");
            System.out.println();
        }
    }

    private List<Integer> printActionMenu(GameEngine engine) {
        List<Integer> availableActions = new ArrayList<Integer>();

        System.out.println("Actions:");

        addActionMenuItem(availableActions, ACTION_TAKE_THREE_DIFFERENT, "Take 3 different gems",
                getTakeThreeDifferentGemsUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_TAKE_TWO_SAME, "Take 2 same gems",
                getTakeTwoSameGemsUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_RESERVE_VISIBLE, "Reserve a visible card",
                getReserveVisibleCardUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_RESERVE_TOP, "Reserve top card from a deck",
                getReserveTopCardUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_BUY_VISIBLE, "Buy a visible card",
                getBuyVisibleCardUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_BUY_RESERVED, "Buy a reserved card",
                getBuyReservedCardUnavailableReason(engine));

        availableActions.add(ACTION_QUIT);
        System.out.println("  " + availableActions.size() + ". Quit");
        return availableActions;
    }

    private void printFinalResults(GameState gameState) {
        System.out.println();
        System.out.println("=== Game Over ===");

        int highestPoints = -1;
        for (Player player : gameState.getPlayers()) {
            if (player.getPoints() > highestPoints) {
                highestPoints = player.getPoints();
            }
        }

        for (Player player : gameState.getPlayers()) {
            System.out.println(player.getName() + " - points: " + player.getPoints()
                    + ", purchased cards: " + player.getPurchasedCards().size()
                    + ", nobles: " + player.getNobles().size());
        }

        System.out.println();
        System.out.println("Winner(s):");
        for (Player player : gameState.getPlayers()) {
            if (player.getPoints() == highestPoints) {
                System.out.println("  " + player.getName());
            }
        }
    }

    private String formatCard(Card card) {
        return "points=" + card.getPoints()
                + ", bonus=" + card.getBonus()
                + ", cost=" + card.getCost();
    }

    private String formatNoble(Noble noble) {
        return noble.getId() + " (points=" + noble.getPoints()
                + ", requirements=" + noble.getRequirements() + ")";
    }

    private String buildBonusSummary(Player player) {
        StringBuilder builder = new StringBuilder();
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(color).append("=").append(player.getBonusCount(color));
        }
        return builder.toString();
    }

    private Tier readTierFromOptions(List<Tier> tierOptions, String prompt) {
        System.out.println("Available tiers:");
        for (int i = 0; i < tierOptions.size(); i++) {
            System.out.println("  " + (i + 1) + ". Tier " + getTierNumber(tierOptions.get(i)));
        }

        int choice = readIntInRange(prompt, 1, tierOptions.size());
        return tierOptions.get(choice - 1);
    }

    private GemColor readGemColorFromOptions(List<GemColor> options, String prompt) {
        System.out.println("Available gem colors:");
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + options.get(i));
        }

        int choice = readIntInRange(prompt, 1, options.size());
        return options.get(choice - 1);
    }

    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Ask again below.
            }

            System.out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    private String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be blank.");
        }
    }

    private int getTierNumber(Tier tier) {
        if (tier == Tier.ONE) {
            return 1;
        }
        if (tier == Tier.TWO) {
            return 2;
        }
        return 3;
    }

    private List<GemColor> getAvailableGemColors(GameEngine engine, int requiredCount, List<GemColor> excludedColors) {
        List<GemColor> options = new ArrayList<GemColor>();
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }
            if (excludedColors.contains(color)) {
                continue;
            }
            if (engine.getGameState().getGemBank().getGemCount(color) >= requiredCount) {
                options.add(color);
            }
        }
        return options;
    }

    private boolean canTakeThreeDifferentGems(GameEngine engine) {
        return getTakeThreeDifferentGemsUnavailableReason(engine) == null;
    }

    private boolean canTakeTwoSameGems(GameEngine engine) {
        return getTakeTwoSameGemsUnavailableReason(engine) == null;
    }

    private boolean canReserveCard(GameEngine engine) {
        return getReserveCardUnavailableReason(engine) == null;
    }

    private boolean canReserveVisibleCard(GameEngine engine) {
        return getReserveVisibleCardUnavailableReason(engine) == null;
    }

    private boolean canReserveTopCard(GameEngine engine) {
        return getReserveTopCardUnavailableReason(engine) == null;
    }

    private boolean canBuyVisibleCard(GameEngine engine) {
        return getBuyVisibleCardUnavailableReason(engine) == null;
    }

    private boolean canBuyReservedCard(GameEngine engine) {
        return getBuyReservedCardUnavailableReason(engine) == null;
    }

    private String getTakeThreeDifferentGemsUnavailableReason(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getTotalGems() + 3 > 10) {
            return "would put you over the 10-token limit";
        }

        if (getAvailableGemColors(engine, 1, new ArrayList<GemColor>()).size() < 3) {
            return "fewer than 3 gem colors are available in the bank";
        }

        return null;
    }

    private String getTakeTwoSameGemsUnavailableReason(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getTotalGems() + 2 > 10) {
            return "would put you over the 10-token limit";
        }

        if (getAvailableGemColors(engine, 4, new ArrayList<GemColor>()).isEmpty()) {
            return "no gem color has at least 4 tokens in the bank";
        }

        return null;
    }

    private String getReserveCardUnavailableReason(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getReservedCards().size() >= 3) {
            return "you already have 3 reserved cards";
        }

        int goldToTake = engine.getGameState().getGemBank().getGemCount(GemColor.GOLD) > 0 ? 1 : 0;
        if (player.getTotalGems() + goldToTake > 10) {
            return "taking the gold token would put you over the 10-token limit";
        }

        return null;
    }

    private String getReserveVisibleCardUnavailableReason(GameEngine engine) {
        String reserveReason = getReserveCardUnavailableReason(engine);
        if (reserveReason != null) {
            return reserveReason;
        }

        if (getTiersWithVisibleCards(engine).isEmpty()) {
            return "there are no visible cards on the board";
        }

        return null;
    }

    private String getReserveTopCardUnavailableReason(GameEngine engine) {
        String reserveReason = getReserveCardUnavailableReason(engine);
        if (reserveReason != null) {
            return reserveReason;
        }

        if (getTiersWithCardsInDeck(engine).isEmpty()) {
            return "all decks are empty";
        }

        return null;
    }

    private String getBuyVisibleCardUnavailableReason(GameEngine engine) {
        if (getTiersWithVisibleCards(engine).isEmpty()) {
            return "there are no visible cards on the board";
        }

        if (getTiersWithAffordableVisibleCards(engine).isEmpty()) {
            return "you cannot afford any visible cards right now";
        }

        return null;
    }

    private String getBuyReservedCardUnavailableReason(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getReservedCards().isEmpty()) {
            return "you do not have any reserved cards";
        }

        if (getAffordableReservedCardIndices(engine).isEmpty()) {
            return "you cannot afford any of your reserved cards right now";
        }

        return null;
    }

    private List<Tier> getTiersWithVisibleCards(GameEngine engine) {
        List<Tier> tiers = new ArrayList<Tier>();
        for (Tier tier : Tier.values()) {
            if (!engine.getGameState().getVisibleCards(tier).isEmpty()) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    private List<Tier> getTiersWithCardsInDeck(GameEngine engine) {
        List<Tier> tiers = new ArrayList<Tier>();
        for (Tier tier : Tier.values()) {
            if (!engine.getGameState().getDeck(tier).isEmpty()) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    private List<Tier> getTiersWithAffordableVisibleCards(GameEngine engine) {
        List<Tier> tiers = new ArrayList<Tier>();
        for (Tier tier : Tier.values()) {
            if (!getAffordableVisibleCardIndices(engine, tier).isEmpty()) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    private List<Integer> getAffordableVisibleCardIndices(GameEngine engine, Tier tier) {
        List<Integer> indices = new ArrayList<Integer>();
        List<Card> visibleCards = engine.getGameState().getVisibleCards(tier);
        for (int i = 0; i < visibleCards.size(); i++) {
            if (engine.canAffordCurrentPlayer(visibleCards.get(i))) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<Integer> getAffordableReservedCardIndices(GameEngine engine) {
        List<Integer> indices = new ArrayList<Integer>();
        List<Card> reservedCards = engine.getCurrentPlayer().getReservedCards();
        for (int i = 0; i < reservedCards.size(); i++) {
            if (engine.canAffordCurrentPlayer(reservedCards.get(i))) {
                indices.add(i);
            }
        }
        return indices;
    }

    private void printCardsWithNumbers(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + formatCard(cards.get(i)));
        }
    }

    private void addActionMenuItem(List<Integer> availableActions, int actionCode,
            String label, String unavailableReason) {
        if (unavailableReason == null) {
            availableActions.add(actionCode);
            System.out.println("  " + availableActions.size() + ". " + label);
        } else {
            System.out.println("  - " + label + " (Unavailable: " + unavailableReason + ")");
        }
    }
}
