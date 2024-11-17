package org.poo.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CardInput;
import org.poo.fileio.Coordinates;

public class Card {
    private final CardInput cardInfo;
    private boolean frozen;
    private boolean hasAttacked;
    private final Coordinates coords;

    public Card(final CardInput card) {
        this.cardInfo = card;
        this.coords = new Coordinates(-1, -1);
        this.frozen = false;
        this.hasAttacked = false;
    }

    /**
     * @return the information about the card given in input
     */
    public CardInput getCardInfo() {
        return cardInfo;
    }

    /**
     * Sets this card as frozen or as not frozen
     * @param frozen is the new frozen status of the card
     */
    public void setFrozen(final boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * @return if the card is frozen or not
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Sets if the card has attacked this round or not
     * @param hasAttacked is the new status of whether the card has attacked
     */
    public void setHasAttacked(final boolean hasAttacked) {
        this.hasAttacked = hasAttacked;
    }

    /**
     * @return true if this card has attacked this round and false if not
     */
    public boolean hasAttacked() {
        return hasAttacked;
    }

    /**
     * Sets new coordinates for the card
     * @param x is the new row of the card on the game table
     * @param y is the new column of the card on the game table
     */
    public void setCoords(final int x, final int y) {
        this.coords.setX(x);
        this.coords.setY(y);
    }

    /**
     * @return the current coordinates from the game table of the card
     */
    public Coordinates getCoords() {
        return coords;
    }

    /**
     * @return true if the card belongs to the back row and false otherwise
     */
    public boolean belongsToBackRow() {
        return this.getCardInfo().getName().equals("Sentinel")
                || this.getCardInfo().getName().equals("Berserker")
                || this.getCardInfo().getName().equals("The Cursed One")
                || this.getCardInfo().getName().equals("Disciple");
    }

    /**
     * @return true if the card belongs to the front row and false otherwise
     */
    public boolean belongsToFrontRow() {
        return this.getCardInfo().getName().equals("Goliath")
                || this.getCardInfo().getName().equals("Warden")
                || this.getCardInfo().getName().equals("The Ripper")
                || this.getCardInfo().getName().equals("Miraj");
    }

    /**
     * Transforms the card's attributes into a JSON representation
     * @param objectMapper an instance of ObjectMapper used to create and manipulate JSON nodes
     * @return an ObjectNode representing the card's attributes
     */
    public ObjectNode cardTransformToAnObjectNode(final ObjectMapper objectMapper) {
        ObjectNode cardNode = objectMapper.createObjectNode();

        if (cardInfo != null) {
            cardNode.put("mana", cardInfo.getMana());
            cardNode.put("attackDamage", cardInfo.getAttackDamage());
            cardNode.put("health", cardInfo.getHealth());
            cardNode.put("description", cardInfo.getDescription());

            if (cardInfo.getColors() != null && !cardInfo.getColors().isEmpty()) {
                ArrayNode colorsArray = objectMapper.createArrayNode();
                for (String color : cardInfo.getColors()) {
                    colorsArray.add(color);
                }
                cardNode.set("colors", colorsArray);
            }
            cardNode.put("name", cardInfo.getName());
        }
        return cardNode;
    }
}
