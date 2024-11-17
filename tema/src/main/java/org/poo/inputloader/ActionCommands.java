package org.poo.inputloader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.ActionsInput;
import org.poo.fileio.Coordinates;
import org.poo.gameprocess.Game;
import org.poo.gameprocess.Player;

public class ActionCommands {
    private final Game game;

    public ActionCommands(final Game game) {
        this.game = game;
    }

    /**
     *
     * @param actionsInput
     * @param objectNode
     * @return
     */
    public boolean execute(final ActionsInput actionsInput, final ObjectNode objectNode) {
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
                    game.getPlayerOne().increaseMana(game.getCurrRound());
                    game.getPlayerTwo().increaseMana(game.getCurrRound());
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
                    Coordinates cardAttacker = actionsInput.getCardAttacker();
                    Coordinates cardAttacked = actionsInput.getCardAttacked();
                    game.attackCard(cardAttacker, cardAttacked);
                } catch (Exception e) {
                    addCardUsesDetails(objectNode, actionsInput, e);
                }
            }
            case "cardUsesAbility" -> {
                try {
                    Coordinates cardAttacker = actionsInput.getCardAttacker();
                    Coordinates cardAttacked = actionsInput.getCardAttacked();
                    game.cardUsesAbility(cardAttacker, cardAttacked);
                } catch (Exception e) {
                    addCardUsesDetails(objectNode, actionsInput, e);
                }
            }
            case "useAttackHero" -> {
                try {
                    game.attackHero(actionsInput.getCardAttacker());
                } catch (Exception e) {
                    if (e.getMessage().equals("Player one killed the enemy hero.")
                            || e.getMessage().equals("Player two killed the enemy hero.")) {
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

    private void addCardUsesDetails(final ObjectNode objectNode, final ActionsInput actionsInput,
                                    final Exception e) {
        objectNode.put("command", actionsInput.getCommand());

        // Add attacker card details
        ObjectNode cardAttackerNode = objectNode.objectNode();
        cardAttackerNode.put("x", actionsInput.getCardAttacker().getX());
        cardAttackerNode.put("y", actionsInput.getCardAttacker().getY());
        objectNode.set("cardAttacker", cardAttackerNode);

        // Add attacked card details
        ObjectNode cardAttackedNode = objectNode.objectNode();
        cardAttackedNode.put("x", actionsInput.getCardAttacked().getX());
        cardAttackedNode.put("y", actionsInput.getCardAttacked().getY());
        objectNode.set("cardAttacked", cardAttackedNode);

        // Add error message
        objectNode.put("error", e.getMessage());
    }

}

