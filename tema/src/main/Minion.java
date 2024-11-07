package main;

import fileio.CardInput;

public class Minion extends Card{

    public Minion() {
    }

    public Minion(CardInput card) {
        super(card);
    }

    // retureaza numarul jucatorului caruia ii apartine
    public int cardBelongsToPlayer() {
        int x = this.getCoords().getX();
        if (x == 0 || x == 1) {
            return 2;
        }
        if (x == 2 || x == 3) {
            return 1;
        }
        return -1;
    }

    public void setHealth(int health) {
        this.getCardInfo().setHealth(health);
    }
    public int getHealth() {
        return this.getCardInfo().getHealth();
    }

    public int getAttackDamage() {
        return getCardInfo().getAttackDamage();
    }
    public void setAttackDamage(int newDamage) {
        getCardInfo().setAttackDamage(newDamage);
    }

    public void decreaseHealth(int attackDamage) {
        int newHealth = this.getCardInfo().getHealth() - attackDamage;
        this.setHealth(newHealth);
    }

    public void increaseHealth(int healthReceived) {
        int newHealth = this.getCardInfo().getHealth() + healthReceived;
        this.setHealth(newHealth);
    }
}
