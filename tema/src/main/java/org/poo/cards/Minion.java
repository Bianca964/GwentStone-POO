package org.poo.cards;

import org.poo.fileio.CardInput;

import static org.poo.gameprocess.Table.PLAYER_ONE_FRONT_ROW;
import static org.poo.gameprocess.Table.PLAYER_ONE_BACK_ROW;
import static org.poo.gameprocess.Table.PLAYER_TWO_FRONT_ROW;
import static org.poo.gameprocess.Table.PLAYER_TWO_BACK_ROW;

public final class Minion extends Card {

    public Minion(final CardInput card) {
        super(card);
    }

    /**
     * Sets a new health to the minion card
     * @param health the new health assigned
     */
    public void setHealth(final int health) {
        this.getCardInfo().setHealth(health);
    }
    public int getHealth() {
        return this.getCardInfo().getHealth();
    }

    /**
     * Sets new attack damage for this minion card
     * @param newDamage the new attack damage of this card
     */
    public void setAttackDamage(final int newDamage) {
        getCardInfo().setAttackDamage(newDamage);
    }
    public int getAttackDamage() {
        return getCardInfo().getAttackDamage();
    }

    /**
     * Decreases health for a minion card
     * @param attackDamage health decreases with this attack damage given
     */
    public void decreaseHealth(final int attackDamage) {
        int newHealth = this.getCardInfo().getHealth() - attackDamage;
        this.setHealth(newHealth);
    }

    /**
     * Increases health for a minion card
     * @param healthReceived health increases with the parameter's value
     */
    public void increaseHealth(final int healthReceived) {
        int newHealth = this.getCardInfo().getHealth() + healthReceived;
        this.setHealth(newHealth);
    }

    /**
     * @return the index of the player to whom the card belongs to
     */
    public int cardBelongsToPlayer() {
        int x = this.getCoords().getX();
        if (x == PLAYER_TWO_FRONT_ROW || x == PLAYER_TWO_BACK_ROW) {
            return 2;
        }
        if (x == PLAYER_ONE_FRONT_ROW || x == PLAYER_ONE_BACK_ROW) {
            return 1;
        }
        // does not belong to any of the players
        return -1;
    }

    /**
     * Checks if a minion card is of type tank or not
     * @return true if the card is tank, and false if it's not
     */
    public boolean isTank() {
        return this.getCardInfo().getName().equals("Goliath")
                || this.getCardInfo().getName().equals("Warden");
    }
}
