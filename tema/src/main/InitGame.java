package main;

import Cards.Hero;
import Cards.Minion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.*;

import java.util.ArrayList;

public class InitGame {
    private final Player playerOne;
    private final Player playerTwo;
    private final ArrayList<GameInput> games;
    private int currGame = 0;
    private final ObjectMapper mapper;
    private final ArrayNode outputArray;

    public InitGame(Input input) {
        this.games = input.getGames();

        Hero hero1 = new Hero(input.getGames().get(this.currGame).getStartGame().getPlayerOneHero());
        Hero hero2 = new Hero(input.getGames().get(this.currGame).getStartGame().getPlayerTwoHero());
        int startingPlayer = input.getGames().get(this.currGame).getStartGame().getStartingPlayer();
        boolean turn1, turn2;
        if (startingPlayer == 1) {
            turn1 = true;
            turn2 = false;
        } else {
            turn1 = false;
            turn2 = true;
        }
        int playerOneDeckIdx = input.getGames().get(currGame).getStartGame().getPlayerOneDeckIdx();
        int playerTwoDeckIdx = input.getGames().get(currGame).getStartGame().getPlayerTwoDeckIdx();

        ArrayList<CardInput> inputPlayerOneDeck = input.getPlayerOneDecks().getDecks().get(playerOneDeckIdx);
        ArrayList<CardInput> playerOneDeck = new ArrayList<>();
        for (CardInput cardInput : inputPlayerOneDeck) {
            playerOneDeck.add(new CardInput(cardInput));
        }

        ArrayList<CardInput> inputPlayerTwoDeck = input.getPlayerTwoDecks().getDecks().get(playerTwoDeckIdx);
        ArrayList<CardInput> playerTwoDeck = new ArrayList<>();
        for (CardInput cardInput : inputPlayerTwoDeck) {
            playerTwoDeck.add(new CardInput(cardInput));
        }

        this.playerOne = new Player(1, playerOneDeck, hero1, turn1);
        this.playerTwo = new Player(2, playerTwoDeck, hero2, turn2);

        this.mapper = new ObjectMapper();
        this.outputArray = mapper.createArrayNode();
    }

    public void debugCommands(Game game, ActionsInput actionsInput, ObjectNode objectNode) {
        objectNode.put("command", actionsInput.getCommand());

        switch (actionsInput.getCommand()) {
            case "getPlayerDeck" -> {
                objectNode.put("playerIdx", actionsInput.getPlayerIdx());
                if (actionsInput.getPlayerIdx() == 1) {
                    objectNode.set("output", playerOne.deckTransformToArrayNode(mapper));
                } else if (actionsInput.getPlayerIdx() == 2) {
                    objectNode.set("output", playerTwo.deckTransformToArrayNode(mapper));
                }
            }
            case "getPlayerHero" -> {
                objectNode.put("playerIdx", actionsInput.getPlayerIdx());
                if (actionsInput.getPlayerIdx() == 1) {
                    objectNode.set("output", playerOne.getHero().heroTransformToAnObjectNode(mapper));
                } else if (actionsInput.getPlayerIdx() == 2) {
                    objectNode.set("output", playerTwo.getHero().heroTransformToAnObjectNode(mapper));
                }
            }
            case "getPlayerTurn" -> {
                if (playerOne.getTurn()) {
                    objectNode.put("output", 1);
                } else if (playerTwo.getTurn()) {
                    objectNode.put("output", 2);
                }
            }
            case "getCardsInHand" -> {
                objectNode.put("playerIdx", actionsInput.getPlayerIdx());
                if (actionsInput.getPlayerIdx() == 1) {
                    objectNode.set("output", playerOne.cardsInHandTransformToArrayNode(mapper));
                } else {
                    objectNode.set("output", playerTwo.cardsInHandTransformToArrayNode(mapper));
                }
            }
            case "getPlayerMana" -> {
                objectNode.put("playerIdx", actionsInput.getPlayerIdx());
                if (actionsInput.getPlayerIdx() == 1) {
                    objectNode.put("output", playerOne.getMana());
                } else {
                    objectNode.put("output", playerTwo.getMana());
                }
            }
            case "getCardsOnTable" -> objectNode.set("output", game.tableTransformToArrayNode(mapper));
            case "getCardAtPosition" -> {
                objectNode.put("x", actionsInput.getX());
                objectNode.put("y", actionsInput.getY());

                Coordinates coords = new Coordinates(actionsInput.getX(), actionsInput.getY());
                Minion card = game.getCardsFromTableWithCoords(coords);
                if (card != null) {
                    objectNode.put("output", card.cardTransformToAnObjectNode(mapper));
                } else {
                    objectNode.put("output", "No card available at that position.");
                }
            }
            case "getFrozenCardsOnTable" -> objectNode.set("output", game.frozenCardsToArrayNode(mapper));
            case "getTotalGamesPlayed" -> objectNode.put("output", currGame + 1);
            case "getPlayerOneWins" -> objectNode.put("output", playerOne.getSuccesses());
            case "getPlayerTwoWins" -> objectNode.put("output", playerTwo.getSuccesses());
        }
    }

    private Boolean actionCommand(final Game game, final ActionsInput actionsInput,
                                  final ObjectNode objectNode) {
        switch (actionsInput.getCommand()) {
            case "endPlayerTurn" -> {
                // marchez ca si a sfarsit tura
                if (game.getPlayerOne().getTurn()) {
                    game.getPlayerOne().setEndedTurn(true);
                } else if (game.getPlayerTwo().getTurn()) {
                    game.getPlayerTwo().setEndedTurn(true);
                }
                game.defrostCards(game.getCurrentPlayer());

                // se termina o runda
                if (game.getPlayerOne().hasEndedTurn() && game.getPlayerTwo().hasEndedTurn()) {
                    game.startNewRound();

                    // incrementez mana jucatorilor
                    game.getPlayerOne().incrementMana(game.getCurrRound());
                    game.getPlayerTwo().incrementMana(game.getCurrRound());
                }
                game.switchTurn();
            }
            case "placeCard" -> {
                int handIdx = actionsInput.getHandIdx();
                try {
                    game.placeCardOnTable(handIdx);
                } catch (Exception e) {
                    objectNode.put("command", actionsInput.getCommand());
                    objectNode.put("handIdx", handIdx);
                    objectNode.put("error", e.getMessage());
                }
            }
            case "cardUsesAttack" -> {
                try {
                    game.attackCard(actionsInput.getCardAttacker(), actionsInput.getCardAttacked());
                } catch (Exception e) {
                    objectNode.put("command", actionsInput.getCommand());

                    // Creăm un ObjectNode pentru coordonatele cardului atacator
                    ObjectNode cardAttackerNode = objectNode.objectNode();
                    cardAttackerNode.put("x", actionsInput.getCardAttacker().getX());
                    cardAttackerNode.put("y", actionsInput.getCardAttacker().getY());
                    objectNode.set("cardAttacker", cardAttackerNode);

                    // Creăm un ObjectNode pentru coordonatele cardului atacat
                    ObjectNode cardAttackedNode = objectNode.objectNode();
                    cardAttackedNode.put("x", actionsInput.getCardAttacked().getX());
                    cardAttackedNode.put("y", actionsInput.getCardAttacked().getY());
                    objectNode.set("cardAttacked", cardAttackedNode);

                    objectNode.put("error", e.getMessage());
                }
            }
            case "cardUsesAbility" -> {
                try {
                    game.cardUsesAbility(actionsInput.getCardAttacker(), actionsInput.getCardAttacked());
                } catch (Exception e) {
                    objectNode.put("command", actionsInput.getCommand());

                    // Creăm un ObjectNode pentru coordonatele cardului atacator
                    ObjectNode cardAttackerNode = objectNode.objectNode();
                    cardAttackerNode.put("x", actionsInput.getCardAttacker().getX());
                    cardAttackerNode.put("y", actionsInput.getCardAttacker().getY());
                    objectNode.set("cardAttacker", cardAttackerNode);

                    // Creăm un ObjectNode pentru coordonatele cardului atacat
                    ObjectNode cardAttackedNode = objectNode.objectNode();
                    cardAttackedNode.put("x", actionsInput.getCardAttacked().getX());
                    cardAttackedNode.put("y", actionsInput.getCardAttacked().getY());
                    objectNode.set("cardAttacked", cardAttackedNode);

                    objectNode.put("error", e.getMessage());
                }
            }
            case "useAttackHero" -> {
                try {
                    game.attackHero(actionsInput.getCardAttacker());
                } catch (Exception e) {
                    if (e.getMessage().equals("Player one killed the enemy hero.") || e.getMessage().equals("Player two killed the enemy hero.")) {
                        objectNode.put("gameEnded", e.getMessage());
                        return true;
                    }
                    objectNode.put("command", actionsInput.getCommand());

                    // Creăm un ObjectNode pentru coordonatele cardului atacator
                    ObjectNode cardAttackerNode = objectNode.objectNode();
                    cardAttackerNode.put("x", actionsInput.getCardAttacker().getX());
                    cardAttackerNode.put("y", actionsInput.getCardAttacker().getY());
                    objectNode.set("cardAttacker", cardAttackerNode);

                    objectNode.put("error", e.getMessage());
                }
            }
            case "useHeroAbility" -> {
                int affectedRow = actionsInput.getAffectedRow();

                Player currrentPlayer;
                if (game.getCurrentPlayer() == 1) {
                    currrentPlayer = game.getPlayerOne();
                } else {
                    currrentPlayer = game.getPlayerTwo();
                }

                try {
                    game.useHeroAbility(affectedRow, currrentPlayer);
                } catch (Exception e) {
                    objectNode.put("command", actionsInput.getCommand());
                    objectNode.put("affectedRow", affectedRow);
                    objectNode.put("error", e.getMessage());
                }
            }
            default -> {
                return false;
            }
        }

        return true;
    }

    public void run(Input input) {
        for (int i = 0; i < games.size(); i++) {
            this.currGame = i;
            GameInput currentGameInput = games.get(i);
            StartGameInput startGameInput = currentGameInput.getStartGame();

            if (i >= 1) {
                Hero hero1 = new Hero(startGameInput.getPlayerOneHero());
                Hero hero2 = new Hero(startGameInput.getPlayerTwoHero());
                int startingPlayer = startGameInput.getStartingPlayer();
                boolean turn1, turn2;
                if (startingPlayer == 1) {
                    turn1 = true;
                    turn2 = false;
                } else {
                    turn1 = false;
                    turn2 = true;
                }
                int playerOneDeckIdx = startGameInput.getPlayerOneDeckIdx();
                int playerTwoDeckIdx = startGameInput.getPlayerTwoDeckIdx();

                ArrayList<CardInput> inputPlayerOneDeck = input.getPlayerOneDecks().getDecks().get(playerOneDeckIdx);
                ArrayList<CardInput> playerOneDeck = new ArrayList<>();
                for (CardInput cardInput : inputPlayerOneDeck) {
                    playerOneDeck.add(new CardInput(cardInput));
                }

                ArrayList<CardInput> inputPlayerTwoDeck = input.getPlayerTwoDecks().getDecks().get(playerTwoDeckIdx);
                ArrayList<CardInput> playerTwoDeck = new ArrayList<>();
                for (CardInput cardInput : inputPlayerTwoDeck) {
                    playerTwoDeck.add(new CardInput(cardInput));
                }

                playerOne.remake(playerOneDeck, hero1, turn1);
                playerTwo.remake(playerTwoDeck, hero2, turn2);
            }

            Game newGame = new Game(playerOne, playerTwo, startGameInput.getShuffleSeed());
            newGame.startNewRound();

            newGame.getPlayerOne().incrementMana(newGame.getCurrRound());
            newGame.getPlayerTwo().incrementMana(newGame.getCurrRound());

            for (ActionsInput actionsInput : currentGameInput.getActions()) {
                ObjectNode objectNode = mapper.createObjectNode();

                if (!this.actionCommand(newGame, actionsInput, objectNode)) {
                    this.debugCommands(newGame, actionsInput, objectNode);
                }

                if (!objectNode.isEmpty()) {
                    this.outputArray.add(objectNode);
                }
            }
        }
    }

    public ArrayNode getOutputArray() {
        return this.outputArray;
    }
}
