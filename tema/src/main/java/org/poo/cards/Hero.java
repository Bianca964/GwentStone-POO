package org.poo.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CardInput;

public final class Hero extends Card {
    public static final int MAX_HEALTH = 30;
    private int health;

    public Hero(final CardInput card) {
        super(card);
        this.health = MAX_HEALTH;
    }

    public int getHealth() {
        return health;
    }
    public void setHealth(final int health) {
        this.health = health;
    }

    /**
     * Decreases the health of the hero with the attackDamage given
     */
    public void decreaseHealth(final int attackDamage) {
        int newHealth = this.health - attackDamage;
        setHealth(newHealth);
    }

    public boolean isLordRoyce() {
        return this.getCardInfo().getName().equals("Lord Royce");
    }

    public boolean isEmpressThorina() {
        return this.getCardInfo().getName().equals("Empress Thorina");
    }

    public boolean isGeneralKocioraw() {
        return this.getCardInfo().getName().equals("General Kocioraw");
    }

    public boolean isKingMudface() {
        return this.getCardInfo().getName().equals("King Mudface");
    }

    @Override
    public ObjectNode cardTransformToAnObjectNode(final ObjectMapper objectMapper) {
        ObjectNode heroNode = objectMapper.createObjectNode();
        CardInput cardInfo = this.getCardInfo();

        if (cardInfo != null) {
            heroNode.put("mana", cardInfo.getMana());
            heroNode.put("description", cardInfo.getDescription());

            if (cardInfo.getColors() != null && !cardInfo.getColors().isEmpty()) {
                ArrayNode colorsArray = objectMapper.createArrayNode();
                for (String color : cardInfo.getColors()) {
                    colorsArray.add(color);
                }
                heroNode.set("colors", colorsArray);
            }
            heroNode.put("name", cardInfo.getName());
        }
        heroNode.put("health", getHealth());
        return heroNode;
    }
}
