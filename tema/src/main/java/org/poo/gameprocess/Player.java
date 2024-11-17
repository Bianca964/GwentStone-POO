package org.poo.gameprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.cards.Card;
import org.poo.cards.Hero;
import org.poo.cards.Minion;
import org.poo.fileio.CardInput;

import java.util.ArrayList;

import static org.poo.gameprocess.Table.TABLE_COLS;

public final class Player {
    private ArrayList<CardInput> deck;
    private int gamesWon;
    private int gamesLost;
    private boolean turn;
    private final int idx;
    private Hero hero;
    private ArrayList<Card> cardsInHand;
    private boolean endedTurnThisRound;
    private int mana;

    public static final int MAX_MANA_RECEIVED = 10;

    public Player(final int idx, final ArrayList<CardInput> deck,
                  final Hero hero, final boolean turn) {
        this.deck = deck;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.turn = turn;
        this.mana = 0;
        this.idx = idx;
        this.hero = hero;
        this.cardsInHand = new ArrayList<>();
        this.endedTurnThisRound = false;
    }

    /**
     * Reinitialize some of the elements of a player (at the beginning of a new game)
     * @param newDeck the new deck the player chose
     * @param newHero the new hero assigned to the player
     * @param newTurn the new status of the player's turn
     */
    public void remake(final ArrayList<CardInput> newDeck, final Hero newHero,
                       final boolean newTurn) {
        this.deck = newDeck;
        this.mana = 0;
        this.turn = newTurn;
        this.hero = newHero;
        this.cardsInHand = new ArrayList<>();
        this.endedTurnThisRound = false;
    }

    /**
     * Marks whether a hero has attacked this round or not
     * @param hasAttacked the new status of the hero's attack
     */
    public void setHeroHasAttacked(final boolean hasAttacked) {
        this.hero.setHasAttacked(hasAttacked);
    }

    /**
     * Decreases the player's hero's health
     * @param attackDamage the number the hero's health decreases with
     */
    public void decreaseHeroHealth(final int attackDamage) {
        this.hero.decreaseHealth(attackDamage);
    }

    /**
     * Takes the first card from the player's deck and put it in player's hand
     */
    public void putCardFromDeckInHand() {
        if (!this.deck.isEmpty()) {
            Card newCard = new Card(this.deck.get(0));
            cardsInHand.add(newCard);
            this.deck.remove(0);
        }
    }

    /**
     * Places a card from the player's hand onto the game table
     * @param handIdx the index of the card in the player's hand ArrayList
     * @param table the game table represented as a 2D array of Minion objects
     * @param frontRow the row index for placing front-row type cards (e.g. Goliath, Warden)
     * @param backRow the row index for placing back-row type cards (e.g. Sentinel, Berserker)
     * @throws Exception if the player doesn't have enough mana to place the card or
     *                   if the designated row on the table is full
     */
    public void placeCardFromHandOnTable(final int handIdx, final Minion[][] table,
                                         final int frontRow, final int backRow) throws Exception {
        if (handIdx >= cardsInHand.size()) {
            return;
        }
        Card cardInHand = cardsInHand.get(handIdx);

        // check if the player has enough mana to place the card on table
        if (cardInHand.getCardInfo().getMana() > this.mana) {
            throw new Exception("Not enough mana to place card on table.");
        }

        Minion minionCard = new Minion(cardInHand.getCardInfo());
        // check what type the card is in order to know on which row to add it
        if (cardInHand.belongsToBackRow()) {
            if (addCardOnTableAtSpecificRow(table, backRow, cardInHand, minionCard)) {
                return; // managed to place the card on table
            }
        } else if (cardInHand.belongsToFrontRow()) {
            if (addCardOnTableAtSpecificRow(table, frontRow, cardInHand, minionCard)) {
                return; // managed to place the card on table
            }
        }

        throw new Exception("Cannot place card on table since row is full.");
    }

    /**
     * Attempts to add a card onto the game table at the specified row
     * @param table the game table represented as a 2D array of Minion objects
     * @param row the index of the row on the table where the card will be placed
     * @param cardInHand the card in the player's hand to be placed on table (needs to be
     *                   transformed into a minion card) and removed from the player's hand
     * @param minionCard the Minion representation of the card to be placed on table
     * @return true if the card was successfully placed on the table, and false if
     *         the row was full and the card could not be placed
     */
    private boolean addCardOnTableAtSpecificRow(final Minion[][] table, final int row,
                                                final Card cardInHand, final Minion minionCard) {
        for (int i = 0; i < TABLE_COLS; i++) {
            if (table[row][i] == null) {
                minionCard.setCoords(row, i);
                table[row][i] = minionCard;
                this.mana -= cardInHand.getCardInfo().getMana();
                cardsInHand.remove(cardInHand);
                return true;
            }
        }
        return false;
    }

    /**
     * Increases the player's mana based on the current round number
     * The player's mana is increased by the value of the current round
     * If the round number exceeds 10, the mana increase is capped at 10
     * @param currRound the number representing the current round of the game
     */
    public void increaseMana(final int currRound) {
        this.mana += Math.min(currRound, MAX_MANA_RECEIVED);
    }

    /**
     * @return the mana of the player
     */
    public int getMana() {
        return this.mana;
    }

    /**
     * Sets the mana of the player with a new mana
     * @param newMana the value of the new mana to be assigned to the player
     */
    public void setMana(final int newMana) {
        this.mana = newMana;
    }

    /**
     * @return the arrayList of the cards the player has in his hand
     */
    public ArrayList<Card> getCardsInHand() {
        return cardsInHand;
    }

    /**
     * Updates the player's turn status to indicate whether they have ended their turn
     * @param endTurnThisRound a boolean value representing the new status
     */
    public void setEndedTurn(final boolean endTurnThisRound) {
        this.endedTurnThisRound = endTurnThisRound;
    }

    /**
     * Checks if the player has ended their turn in the current round
     * @return true if the player has ended their turn, false otherwise
     */
    public boolean hasEndedTurn() {
        return this.endedTurnThisRound;
    }

    /**
     * @return the index of the player (1 for player one and 2 for player two)
     */
    public int getIdx() {
        return this.idx;
    }

    /**
     * Sets the player's turn status
     * @param turn a boolean value indicating whether it is the player's turn
     */
    public void setTurn(final boolean turn) {
        this.turn = turn;
    }

    /**
     * Checks if it is the player's turn
     * @return true if it is the player's turn, false otherwise
     */
    public boolean getTurn() {
        return this.turn;
    }

    /**
     * @return the player's deck
     */
    public ArrayList<CardInput> getDeck() {
        return this.deck;
    }

    /**
     * @return the number of games won by the player
     */
    public int getSuccesses() {
        return this.gamesWon;
    }

    /**
     * Increment the wins of the player
     */
    public void incrementWin() {
        this.gamesWon++;
    }

    /**
     * Increment the losses of the player
     */
    public void incrementLoss() {
        this.gamesLost++;
    }

    /**
     * Sets the player's hero
     * @param hero the hero to be assigned to the player
     */
    public void setHero(final Hero hero) {
        this.hero = hero;
    }

    /**
     * @return the hero assigned to the player
     */
    public Hero getHero() {
        return this.hero;
    }

    /**
     * Transforms the player's cards in hand into a JSON array node
     * @param objectMapper the ObjectMapper used for creating JSON nodes
     * @return an ArrayNode representing the cards in the player's hand
     */
    public ArrayNode cardsInHandTransformToArrayNode(final ObjectMapper objectMapper) {
        ArrayNode handNode = objectMapper.createArrayNode();
        for (Card card : cardsInHand) {
            ObjectNode cardNode = card.cardTransformToAnObjectNode(objectMapper);
            handNode.add(cardNode);
        }
        return handNode;
    }

    /**
     * Transforms the player's deck into a JSON ArrayNode
     * @param objectMapper the ObjectMapper used for creating JSON nodes
     * @return an ArrayNode containing the serialized representation of the player's deck
     */
    public ArrayNode deckTransformToArrayNode(final ObjectMapper objectMapper) {
        ArrayNode deckNode = objectMapper.createArrayNode();
        for (CardInput cardInput : deck) {
            Card card = new Card(cardInput);
            deckNode.add(card.cardTransformToAnObjectNode(objectMapper));
        }
        return deckNode;
    }
}
