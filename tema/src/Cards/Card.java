package Cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;
import fileio.Coordinates;

public class Card {
    private CardInput cardInfo;
    private boolean frozen;
    private boolean hasAttacked;
    private Coordinates coords;

    public Card() {
    }

    public Card(CardInput card) {
        this.cardInfo = card;
        this.coords = new Coordinates(-1, -1);
        this.frozen = false;
        this.hasAttacked = false;
    }

    public void setCard(CardInput card) {
        this.cardInfo = card;
    }
    public CardInput getCardInfo() {
        return cardInfo;
    }
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }
    public boolean isFrozen() {
        return frozen;
    }
    public void setHasAttacked(boolean hasAttacked) {
        this.hasAttacked = hasAttacked;
    }
    public boolean hasAttacked() {
        return hasAttacked;
    }

    public void setCoords(int x, int y) {
        this.coords.setX(x);
        this.coords.setY(y);
    }
    public Coordinates getCoords() {
        return coords;
    }

    public ObjectNode cardTransformToAnObjectNode(ObjectMapper objectMapper) {
        // Cream un ObjectNode
        ObjectNode cardNode = objectMapper.createObjectNode();

        // Adăugăm informațiile din cardInfo
        if (cardInfo != null) {
            cardNode.put("mana", cardInfo.getMana());
            cardNode.put("attackDamage", cardInfo.getAttackDamage());
            cardNode.put("health", cardInfo.getHealth());
            cardNode.put("description", cardInfo.getDescription());

            // Adăugăm culorile ca un ArrayNode dacă sunt disponibile
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

