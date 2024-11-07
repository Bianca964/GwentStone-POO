package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CardInput;

public class Hero extends Card{
    private int health;

    public Hero(CardInput card) {
        super(card);
        this.health = 30;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void decreaseHealth(int attackDamage) {
        int newHealth = this.health - attackDamage;
        setHealth(newHealth);
    }

    public boolean isLordRoyce() {
        if (this.getCardInfo().getName().equals("Lord Royce")) {
            return true;
        }
        return false;
    }

    public boolean isEmpressThorina() {
        if (this.getCardInfo().getName().equals("Empress Thorina")) {
            return true;
        }
        return false;
    }

    public boolean isGeneralKocioraw () {
        if (this.getCardInfo().getName().equals("General Kocioraw")) {
            return true;
        }
        return false;
    }

    public boolean isKingMudface (){
        if (this.getCardInfo().getName().equals("King Mudface")) {
            return true;
        }
        return false;
    }

    public ObjectNode heroTransformToAnObjectNode(ObjectMapper objectMapper) {
        // Creăm un ObjectNode pentru erou
        ObjectNode heroNode = objectMapper.createObjectNode();

        // Extragem cardInfo din obiectul curent
        CardInput cardInfo = this.getCardInfo(); // Folosim metoda getCard() pentru a accesa cardInfo

        // Adăugăm informațiile din cardInfo (superclasa Card ar trebui să aibă cardInfo)
        if (cardInfo != null) {
            heroNode.put("mana", cardInfo.getMana());
            heroNode.put("description", cardInfo.getDescription());

            // Adăugăm culorile ca un ArrayNode dacă sunt disponibile
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
