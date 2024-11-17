package org.poo.inputloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.cards.Minion;
import org.poo.fileio.ActionsInput;
import org.poo.fileio.Coordinates;
import org.poo.gameprocess.Game;
import org.poo.gameprocess.Player;

public final class DebugCommands {
    private final Player playerOne;
    private final Player playerTwo;
    private final int currGame;
    private final ObjectMapper mapper;

    public DebugCommands(final Player playerOne, final Player playerTwo,
                         final int currGame, final ObjectMapper mapper) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.currGame = currGame;
        this.mapper = mapper;
    }

    /**
     * Executes a command based on the input action and modifies the given JSON object
     * with the result of the operation
     * @param game the current game instance containing the state and logic of the game
     * @param actionsInput the input describing the action to be performed, including
     *                     command and parameters
     * @param objectNode the JSON object that will be modified to include the command's result
     */
    public void execute(final Game game, final ActionsInput actionsInput,
                        final ObjectNode objectNode) {
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
                    objectNode.set("output",
                                    playerOne.getHero().cardTransformToAnObjectNode(mapper));
                } else if (actionsInput.getPlayerIdx() == 2) {
                    objectNode.set("output",
                                    playerTwo.getHero().cardTransformToAnObjectNode(mapper));
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
            case "getCardsOnTable" -> objectNode.set("output",
                                                     game.tableTransformToArrayNode(mapper));
            case "getCardAtPosition" -> {
                objectNode.put("x", actionsInput.getX());
                objectNode.put("y", actionsInput.getY());

                Coordinates coords = new Coordinates(actionsInput.getX(), actionsInput.getY());
                Minion card = game.getCardsFromTableWithCoords(coords);
                if (card != null) {
                    objectNode.set("output", card.cardTransformToAnObjectNode(mapper));
                } else {
                    objectNode.put("output", "No card available at that position.");
                }
            }
            case "getFrozenCardsOnTable" -> objectNode.set("output",
                                                            game.frozenCardsToArrayNode(mapper));
            case "getTotalGamesPlayed" -> objectNode.put("output", currGame + 1);
            case "getPlayerOneWins" -> objectNode.put("output", playerOne.getSuccesses());
            case "getPlayerTwoWins" -> objectNode.put("output", playerTwo.getSuccesses());

            default -> {
                return;
            }
        }
    }
}
