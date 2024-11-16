package inputloader;

import cards.Hero;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.Input;
import fileio.ActionsInput;
import fileio.GameInput;
import fileio.CardInput;
import fileio.StartGameInput;
import gameprocess.Game;
import gameprocess.Player;

import java.util.ArrayList;

public class InitGame {
    private final Player playerOne;
    private final Player playerTwo;
    private final ArrayList<GameInput> games;
    private int currGame = 0;
    private final ObjectMapper mapper;
    private final ArrayNode outputArray;

    public InitGame(final Input input) {
        this.games = input.getGames();

        Hero hero1 = new Hero(input.getGames().get(currGame).getStartGame().getPlayerOneHero());
        Hero hero2 = new Hero(input.getGames().get(currGame).getStartGame().getPlayerTwoHero());
        int startingPlayer = input.getGames().get(currGame).getStartGame().getStartingPlayer();
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

        ArrayList<ArrayList<CardInput>> playerOneDecks = input.getPlayerOneDecks().getDecks();
        ArrayList<CardInput> inputPlayerOneDeck = playerOneDecks.get(playerOneDeckIdx);
        ArrayList<CardInput> playerOneDeck = new ArrayList<>();
        for (CardInput cardInput : inputPlayerOneDeck) {
            playerOneDeck.add(new CardInput(cardInput));
        }

        ArrayList<ArrayList<CardInput>> playerTwoDecks = input.getPlayerTwoDecks().getDecks();
        ArrayList<CardInput> inputPlayerTwoDeck = playerTwoDecks.get(playerTwoDeckIdx);
        ArrayList<CardInput> playerTwoDeck = new ArrayList<>();
        for (CardInput cardInput : inputPlayerTwoDeck) {
            playerTwoDeck.add(new CardInput(cardInput));
        }

        this.playerOne = new Player(1, playerOneDeck, hero1, turn1);
        this.playerTwo = new Player(2, playerTwoDeck, hero2, turn2);

        this.mapper = new ObjectMapper();
        this.outputArray = mapper.createArrayNode();
    }

    /**
     * Executes a sequence of games based on the provided input, processing each game's
     * setup, actions, and debug commands
     * @param input the overall input data for the games
     */
    public void run(final Input input) {
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

                ArrayList<CardInput> inputPlayerOneDeck;
                inputPlayerOneDeck = input.getPlayerOneDecks().getDecks().get(playerOneDeckIdx);
                ArrayList<CardInput> playerOneDeck = new ArrayList<>();
                for (CardInput cardInput : inputPlayerOneDeck) {
                    playerOneDeck.add(new CardInput(cardInput));
                }

                ArrayList<CardInput> inputPlayerTwoDeck;
                inputPlayerTwoDeck = input.getPlayerTwoDecks().getDecks().get(playerTwoDeckIdx);
                ArrayList<CardInput> playerTwoDeck = new ArrayList<>();
                for (CardInput cardInput : inputPlayerTwoDeck) {
                    playerTwoDeck.add(new CardInput(cardInput));
                }

                playerOne.remake(playerOneDeck, hero1, turn1);
                playerTwo.remake(playerTwoDeck, hero2, turn2);
            }

            Game newGame = new Game(playerOne, playerTwo, startGameInput.getShuffleSeed());
            newGame.startNewRound();

            newGame.getPlayerOne().increaseMana(newGame.getCurrRound());
            newGame.getPlayerTwo().increaseMana(newGame.getCurrRound());

            DebugCommands debugCommands = new DebugCommands(playerOne, playerTwo, currGame, mapper);
            ActionCommands actionCommands = new ActionCommands(newGame);

            for (ActionsInput actionsInput : currentGameInput.getActions()) {
                ObjectNode objectNode = mapper.createObjectNode();

                if (!actionCommands.execute(actionsInput, objectNode)) {
                    debugCommands.execute(newGame, actionsInput, objectNode);
                }
                if (!objectNode.isEmpty()) {
                    this.outputArray.add(objectNode);
                }
            }
        }
    }

    /**
     * @return the resulted output array after processing all commands
     */
    public ArrayNode getOutputArray() {
        return this.outputArray;
    }
}
