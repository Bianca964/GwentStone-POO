package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import Cards.Card;
import Cards.Hero;
import Cards.Minion;

import java.util.ArrayList;

public class Player {
    private ArrayList<CardInput> deck;
    private int gamesWon;
    private int gamesLost;
    private boolean turn;
    private final int idx;
    private Hero hero;
    private ArrayList<Card> cardsInHand;
    private boolean endedTurnThisRound;
    private int mana;

    public Player(int idx, ArrayList<CardInput> deck, Hero hero, boolean turn) {
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

    public void remake(ArrayList<CardInput> deck, Hero hero, boolean turn) {
        this.deck = deck;
        this.mana = 0;
        this.turn = turn;
        this.hero = hero;
        this.cardsInHand = new ArrayList<>();
        this.endedTurnThisRound = false;
    }

    public void setHeroHasAttacked(boolean hasAttacked) {
        this.hero.setHasAttacked(hasAttacked);
    }

    public void decreaseHeroHealth(int attackDamage) {
        this.hero.decreaseHealth(attackDamage);
    }

    public void putCardFromDeckInHand() {
        if (!this.deck.isEmpty()) {
            Card newCard = new Card(this.deck.get(0));
            cardsInHand.add(newCard);
            this.deck.remove(0);
        }
    }

    public void placeCardFromHandOnTable(int handIdx, Minion[][] table, int frontRow, int backRow) throws Exception {
        if (handIdx >= cardsInHand.size()) {
            return;
        }
        Card cardInHand = cardsInHand.get(handIdx);

        // tb sa aiba suficienta mana pentru a l adauga pe masa
        if (cardInHand.getCardInfo().getMana() > this.mana) {
            throw new Exception("Not enough mana to place card on table.");
        }

        Minion minionCard = new Minion(cardInHand.getCardInfo());
        // tb sa vad de ce tip e ca sa stiu pe ce rand o adaug
        if (cardInHand.getCardInfo().getName().equals("Sentinel")
                || cardInHand.getCardInfo().getName().equals("Berserker")
                || cardInHand.getCardInfo().getName().equals("The Cursed One")
                || cardInHand.getCardInfo().getName().equals("Disciple")) {
            if (addCardOnTableAtSpecificRow(table, backRow, cardInHand, minionCard)) return;
        } else if (cardInHand.getCardInfo().getName().equals("Goliath")
                || cardInHand.getCardInfo().getName().equals("Warden")
                || cardInHand.getCardInfo().getName().equals("The Ripper")
                || cardInHand.getCardInfo().getName().equals("Miraj")) {
            if (addCardOnTableAtSpecificRow(table, frontRow, cardInHand, minionCard)) return; // am reusit sa o plasez  pe masa
        }

        throw new Exception("Cannot place card on table since row is full.");
    }

    private boolean addCardOnTableAtSpecificRow(Minion[][] table, int frontRow, Card cardInHand, Minion minionCard) {
        for (int i = 0; i < 5; i++) {
            if (table[frontRow][i] == null) {
                minionCard.setCoords(frontRow, i);
                table[frontRow][i] = minionCard;
                this.mana -= cardInHand.getCardInfo().getMana();
                cardsInHand.remove(cardInHand);
                return true;
            }
        }
        return false;
    }

    public void incrementMana(int currRound) {
        this.mana += Math.min(currRound, 10);
    }

    public int getMana() {
        return this.mana;
    }
    public void setMana(int newMana) {
        this.mana = newMana;
    }
    public ArrayList<Card> getCardsInHand() {
        return cardsInHand;
    }

    public void setEndedTurn(boolean endedTurnThisRound) {
        this.endedTurnThisRound = endedTurnThisRound;
    }
    public boolean hasEndedTurn() {
        return this.endedTurnThisRound;
    }

    public int getIdx() {
        return this.idx;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }
    public boolean getTurn() {
        return this.turn;
    }

    public ArrayList<CardInput> getDeck() {
        return this.deck;
    }

    public int getSuccesses() {
        return this.gamesWon;
    }


    public void incrementWin() {
        this.gamesWon++;
    }
    public void incrementLoss() {
        this.gamesLost++;
    }

    public void setHero(Hero hero) {
        this.hero = hero;
    }
    public Hero getHero() {
        return this.hero;
    }

    public ArrayNode cardsInHandTransformToArrayNode(ObjectMapper objectMapper) {
        // Cream un ArrayNode pentru cartile din mana jucatorului
        ArrayNode handNode = objectMapper.createArrayNode();
        for (Card card : cardsInHand) {
            // Folosim metoda cardTransformToAnObjectNode pentru a crea un ObjectNode
            ObjectNode cardNode = card.cardTransformToAnObjectNode(objectMapper);
            // Adăugăm cardul transformat la lista de carti
            handNode.add(cardNode);
        }
        return handNode;
    }

    // Metoda de transformare a deck-ului într-un ArrayNode
    public ArrayNode deckTransformToArrayNode(ObjectMapper objectMapper) {
        // Cream un ArrayNode pentru deck-ul actual al jucatorului
        ArrayNode deckNode = objectMapper.createArrayNode();
        for (CardInput cardInput : deck) {
            Card card = new Card(cardInput);
            deckNode.add(card.cardTransformToAnObjectNode(objectMapper));
        }
        return deckNode;
    }
}
