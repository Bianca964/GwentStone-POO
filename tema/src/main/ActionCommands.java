package main;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.ActionsInput;

public class ActionCommands {
    private final Game game;

    public ActionCommands(Game game) {
        this.game = game;
    }

    public boolean execute(ActionsInput actionsInput, ObjectNode objectNode) {
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
}

