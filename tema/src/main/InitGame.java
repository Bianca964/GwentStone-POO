package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.*;

import java.util.ArrayList;

public class InitGame {
    private Player playerOne;
    private Player playerTwo;
    private ArrayList<GameInput> games = new ArrayList<>();
    private int currGame = 0;
    private ObjectMapper mapper;
    private ArrayNode outputArray;

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
        for (int j = 0; j < inputPlayerOneDeck.size(); j++) {
            playerOneDeck.add(new CardInput(inputPlayerOneDeck.get(j)));
        }

        ArrayList<CardInput> inputPlayerTwoDeck = input.getPlayerTwoDecks().getDecks().get(playerTwoDeckIdx);
        ArrayList<CardInput> playerTwoDeck = new ArrayList<>();
        for (int j = 0; j < inputPlayerTwoDeck.size(); j++) {
            playerTwoDeck.add(new CardInput(inputPlayerTwoDeck.get(j)));
        }

        this.playerOne = new Player(1, playerOneDeck, hero1, turn1);
        this.playerTwo = new Player(2, playerTwoDeck, hero2, turn2);

        this.mapper = new ObjectMapper();
        this.outputArray = mapper.createArrayNode();
    }

    public void debugCommands(Game game, ActionsInput actionsInput, ObjectNode objectNode) {
        objectNode.put("command", actionsInput.getCommand());

        if (actionsInput.getCommand().equals("getPlayerDeck")) {
            objectNode.put("playerIdx", actionsInput.getPlayerIdx());
            if (actionsInput.getPlayerIdx() == 1) {
                objectNode.set("output", playerOne.deckTransformToArrayNode(mapper));
            } else if (actionsInput.getPlayerIdx() == 2) {
                objectNode.set("output", playerTwo.deckTransformToArrayNode(mapper));
            }

        } else if (actionsInput.getCommand().equals("getPlayerHero")) {
            objectNode.put("playerIdx", actionsInput.getPlayerIdx());
            if (actionsInput.getPlayerIdx() == 1) {
                objectNode.set("output", playerOne.getHero().heroTransformToAnObjectNode(mapper));
            } else if (actionsInput.getPlayerIdx() == 2) {
                objectNode.set("output", playerTwo.getHero().heroTransformToAnObjectNode(mapper));
            }

        } else if (actionsInput.getCommand().equals("getPlayerTurn")) {
            if (playerOne.getTurn()) {
                objectNode.put("output", 1);
            } else if (playerTwo.getTurn()) {
                objectNode.put("output", 2);
            }

        } else if (actionsInput.getCommand().equals("getCardsInHand")) {
            objectNode.put("playerIdx", actionsInput.getPlayerIdx());
            if (actionsInput.getPlayerIdx() == 1) {
                objectNode.set("output", playerOne.cardsInHandTransformToArrayNode(mapper));
            } else {
                objectNode.set("output", playerTwo.cardsInHandTransformToArrayNode(mapper));
            }

        } else if (actionsInput.getCommand().equals("getPlayerMana")) {
            objectNode.put("playerIdx", actionsInput.getPlayerIdx());
            if (actionsInput.getPlayerIdx() == 1) {
                objectNode.put("output", playerOne.getMana());
            } else {
                objectNode.put("output", playerTwo.getMana());
            }

        } else if (actionsInput.getCommand().equals("getCardsOnTable")) {
            objectNode.set("output", game.tableTransformToArrayNode(mapper));

        } else if (actionsInput.getCommand().equals("getCardAtPosition")) {
            objectNode.put("x", actionsInput.getX());
            objectNode.put("y", actionsInput.getY());

            Coordinates coords = new Coordinates(actionsInput.getX(), actionsInput.getY());
            Minion card = game.getCardsFromTableWithCoords(coords);
            if (card != null) {
                objectNode.put("output", card.cardTransformToAnObjectNode(mapper));
            } else {
                objectNode.put("output", "No card available at that position.");
            }
        } else if (actionsInput.getCommand().equals("getFrozenCardsOnTable")) {
            objectNode.set("output", game.frozenCardsToArrayNode(mapper));

        } else if (actionsInput.getCommand().equals("getTotalGamesPlayed")) {
            objectNode.put("output", currGame + 1);

        } else if (actionsInput.getCommand().equals("getPlayerOneWins")) {
            objectNode.put("output", playerOne.getSuccesses());

        } else if (actionsInput.getCommand().equals("getPlayerTwoWins")) {
            objectNode.put("output", playerTwo.getSuccesses());
        }
    }

    private Boolean actionCommand(final Game game, final ActionsInput actionsInput,
                                  final ObjectNode objectNode) {
            // END PLAYER TURN
        if (actionsInput.getCommand().equals("endPlayerTurn")) {
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

            // PLACE CARD (FROM HAND ON TABLE)
        } else if (actionsInput.getCommand().equals("placeCard")) {
            int handIdx = actionsInput.getHandIdx();
            try {
                game.placeCardOnTable(handIdx);
            } catch (Exception e) {
                objectNode.put("command", actionsInput.getCommand());
                objectNode.put("handIdx", handIdx);
                objectNode.put("error", e.getMessage());
            }

            // CARD USES ATTACK
        } else if (actionsInput.getCommand().equals("cardUsesAttack")) {
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

            // CARD USES ABILITY
        } else if (actionsInput.getCommand().equals("cardUsesAbility")) {
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

            // USE ATTACK HERO
        } else if (actionsInput.getCommand().equals("useAttackHero")) {
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

            // USE HERO ABILITY
        } else if (actionsInput.getCommand().equals("useHeroAbility")) {
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
        } else {
            return false;
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
                for (int j = 0; j < inputPlayerOneDeck.size(); j++) {
                    playerOneDeck.add(new CardInput(inputPlayerOneDeck.get(j)));
                }

                ArrayList<CardInput> inputPlayerTwoDeck = input.getPlayerTwoDecks().getDecks().get(playerTwoDeckIdx);
                ArrayList<CardInput> playerTwoDeck = new ArrayList<>();
                for (int j = 0; j < inputPlayerTwoDeck.size(); j++) {
                    playerTwoDeck.add(new CardInput(inputPlayerTwoDeck.get(j)));
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
