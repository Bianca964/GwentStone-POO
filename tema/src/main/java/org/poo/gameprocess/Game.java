package org.poo.gameprocess;

import org.poo.cards.Minion;
import org.poo.fileio.Coordinates;

import java.util.Collections;
import java.util.Random;

public class Game extends Table {
    private final Player playerOne;
    private final Player playerTwo;
    private int currentPlayer;
    private int currRound;

    public Game(final Player playerOne, final Player playerTwo, final int shuffleSeed) {
        super(); // constructor for table
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.currentPlayer = playerOne.getTurn() ? 1 : 2;
        this.currRound = 0;

        Collections.shuffle(playerOne.getDeck(), new Random(shuffleSeed));
        Collections.shuffle(playerTwo.getDeck(), new Random(shuffleSeed));
    }

    /**
     * @return the index of the player whose turn is
     */
    public int getCurrentPlayer() {
        return currentPlayer == 1 ? 1 : 2;
    }

    /**
     * Switch turn between the two players
     */
    public void switchTurn() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        playerOne.setTurn(!playerOne.getTurn());
        playerTwo.setTurn(!playerTwo.getTurn());
    }

    /**
     * @return player one
     */
    public Player getPlayerOne() {
        return this.playerOne;
    }

    /**
     * @return player two
     */
    public Player getPlayerTwo() {
        return this.playerTwo;
    }

    /**
     * Sets up the environment for a new round
     */
    public void startNewRound() {
        this.currRound++;
        playerOne.putCardFromDeckInHand();
        playerTwo.putCardFromDeckInHand();
        playerOne.setEndedTurn(false);
        playerTwo.setEndedTurn(false);

        // mark cards on table as "has not attacked" at the beginning of the round
        this.markCardsHasNotAttacked();

        // mark hero as "has not attacked"
        playerOne.setHeroHasAttacked(false);
        playerTwo.setHeroHasAttacked(false);
    }

    /**
     * Places a card from the current player's hand onto the game table
     * The action is performed based on the current player's turn and
     * the specified index of the card in their hand
     * @param handIdx the index of the card in the player's hand that needs to be placed on table
     * @throws Exception if the action cannot be completed, such as due to invalid card index,
     * insufficient mana, or other game-specific rules
     */
    public void placeCardOnTable(final int handIdx) throws Exception {
        // place the cards of the current player (whose turn is) on table
        if (playerOne.getTurn()) {
            playerOne.placeCardFromHandOnTable(handIdx, this.getTable(),
                                               PLAYER_ONE_FRONT_ROW, PLAYER_ONE_BACK_ROW);
        } else {
            playerTwo.placeCardFromHandOnTable(handIdx, this.getTable(),
                                               PLAYER_TWO_FRONT_ROW, PLAYER_TWO_BACK_ROW);
        }
    }

    /**
     * Executes an attack action where one card on the table attacks another
     * @param coordsCardAttacker the coordinates of the attacking card on the table
     * @param coordsCardAttacked the coordinates of the attacked card on the table
     * @throws Exception if the attack cannot be performed due to any of the conditions
     */
    public void attackCard(final Coordinates coordsCardAttacker,
                           final Coordinates coordsCardAttacked) throws Exception {
        Minion cardAttacker = getCardsFromTableWithCoords(coordsCardAttacker);
        Minion cardAttacked = getCardsFromTableWithCoords(coordsCardAttacked);

        int attackerIdx = cardAttacker.cardBelongsToPlayer();
        int attackedIdx = cardAttacked.cardBelongsToPlayer();

        if (attackerIdx == attackedIdx) {
            throw new Exception("Attacked card does not belong to the enemy.");
        }
        if (cardAttacker.hasAttacked()) {
            throw new Exception("Attacker card has already attacked this turn.");
        }
        if (cardAttacker.isFrozen()) {
            throw new Exception("Attacker card is frozen.");
        }

        // check if there is a tank card on the enemy's rows
        Minion tank = this.existsTankOnEnemyRows(attackedIdx);
        if (tank != null) {
            if (!cardAttacked.isTank()) {
                throw new Exception("Attacked card is not of type 'Tank'.");
            }
        }

        // attack the card
        cardAttacked.decreaseHealth(cardAttacker.getAttackDamage());
        // remove the card from table if the card's health reaches 0 after the attack
        if (cardAttacked.getHealth() <= 0) {
            this.removeCardFromTable(cardAttacked.getCoords());
        }

        cardAttacker.setHasAttacked(true);
    }

    /**
     * Executes an ability action where a card uses its special ability on another card
     * @param coordsCardAttacker the coordinates of the card using its ability
     * @param coordsCardAttacked the coordinates of the card being targeted by the ability
     * @throws Exception if the ability cannot be executed due to any of the conditions
     */
    public void cardUsesAbility(final Coordinates coordsCardAttacker,
                                final Coordinates coordsCardAttacked) throws Exception {
        Minion cardAttacker = getCardsFromTableWithCoords(coordsCardAttacker);
        Minion cardAttacked = getCardsFromTableWithCoords(coordsCardAttacked);

        int attackerIdx = cardAttacker.cardBelongsToPlayer();
        int attackedIdx = cardAttacked.cardBelongsToPlayer();

        if (cardAttacker.isFrozen()) {
            throw new Exception("Attacker card is frozen.");
        }
        if (cardAttacker.hasAttacked()) {
            throw new Exception("Attacker card has already attacked this turn.");
        }

        if (cardAttacker.getCardInfo().getName().equals("Disciple")) {
            if (attackerIdx != attackedIdx) {
                throw new Exception("Attacked card does not belong to the current player.");
            }
            // the card belongs to the attacker
            cardAttacked.increaseHealth(2);
            cardAttacker.setHasAttacked(true);
            return;
        }

        // if reached here, the card has one of the other 3 abilities
        // check if the attacked card belongs to the enemy
        if (attackerIdx == attackedIdx) {
            throw new Exception("Attacked card does not belong to the enemy.");
        }

        // check if there is a tank card on the enemy's rows
        Minion tank = this.existsTankOnEnemyRows(attackedIdx);
        if (tank != null) {
            // check if the attacked card is of type tank
            if (!cardAttacked.isTank()) {
                throw new Exception("Attacked card is not of type 'Tank'.");
            }
        }

        if (cardAttacker.getCardInfo().getName().equals("The Ripper")) {
            int newAttackDamage = cardAttacked.getAttackDamage() - 2;
            if (newAttackDamage < 2) {
                newAttackDamage = 0;
            }
            cardAttacked.setAttackDamage(newAttackDamage);
        }

        if (cardAttacker.getCardInfo().getName().equals("Miraj")) {
            int cardAttackedHealth = cardAttacked.getHealth();
            int cardAttackerHealth = cardAttacker.getHealth();

            cardAttacked.setHealth(cardAttackerHealth);
            cardAttacker.setHealth(cardAttackedHealth);
        }

        if (cardAttacker.getCardInfo().getName().equals("The Cursed One")) {
            int attackDamage = cardAttacked.getAttackDamage();
            int health = cardAttacked.getHealth();

            cardAttacked.setHealth(attackDamage);
            cardAttacked.setAttackDamage(health);

            // remove the card from table if the card's health reaches 0 after the attack
            if (cardAttacked.getHealth() <= 0) {
                this.removeCardFromTable(cardAttacked.getCoords());
            }
        }

        cardAttacker.setHasAttacked(true);
    }

    /**
     * Executes an attack action where a minion card attacks the enemy hero
     * @param coordsCardAttacker the coordinates of the card attacking the enemy hero
     * @throws Exception if the attack cannot be executed due to any of the conditions
     */
    public void attackHero(final Coordinates coordsCardAttacker) throws Exception {
        Minion cardAttacker = getCardsFromTableWithCoords(coordsCardAttacker);
        int attackerIdx = cardAttacker.cardBelongsToPlayer();
        int attackedIdx;
        if (attackerIdx == 1) {
            attackedIdx = 2;
        } else {
            attackedIdx = 1;
        }

        if (cardAttacker.isFrozen()) {
            throw new Exception("Attacker card is frozen.");
        }
        if (cardAttacker.hasAttacked()) {
            throw new Exception("Attacker card has already attacked this turn.");
        }

        // check if there is a tank card on the enemy's rows
        Minion tank = this.existsTankOnEnemyRows(attackedIdx);
        if (tank != null) {
            throw new Exception("Attacked card is not of type 'Tank'.");
        }

        // attack the enemy's hero
        if (attackedIdx == 1) {
            playerOne.decreaseHeroHealth(cardAttacker.getAttackDamage());
            if (playerOne.getHero().getHealth() <= 0) {
                playerOne.incrementLoss();
                playerTwo.incrementWin();
                throw new Exception("Player two killed the enemy hero.");
            }
        } else {
            playerTwo.decreaseHeroHealth(cardAttacker.getAttackDamage());
            if (playerTwo.getHero().getHealth() <= 0) {
                playerTwo.incrementLoss();
                playerOne.incrementWin();
                throw new Exception("Player one killed the enemy hero.");
            }
        }

        cardAttacker.setHasAttacked(true);
    }

    /**
     * Executes the hero's special ability on a specific row of the game table
     * The behavior of the ability depends on the type of hero being used
     * @param affectedRow the row of the table where the hero's ability will be applied
     * @param player the player using the hero's ability
     * @throws Exception if the ability cannot be executed due to any of the conditions
     */
    public void useHeroAbility(final int affectedRow, final Player player) throws Exception {
        if (player.getMana() < player.getHero().getCardInfo().getMana()) {
            throw new Exception("Not enough mana to use hero's ability.");
        }
        if (player.getHero().hasAttacked()) {
            throw new Exception("Hero has already attacked this turn.");
        }
        if (player.getHero().isLordRoyce() || player.getHero().isEmpressThorina()) {
            if ((player.getIdx() == 1 && (affectedRow == PLAYER_ONE_FRONT_ROW
                    || affectedRow == PLAYER_ONE_BACK_ROW))
                    || (player.getIdx() == 2 && (affectedRow == PLAYER_TWO_FRONT_ROW
                    || affectedRow == PLAYER_TWO_BACK_ROW))) {
                throw new Exception("Selected row does not belong to the enemy.");
            }
        }
        if (player.getHero().isGeneralKocioraw() || player.getHero().isKingMudface()) {
            if ((player.getIdx() == 1 && (affectedRow == PLAYER_TWO_FRONT_ROW
                    || affectedRow == PLAYER_TWO_BACK_ROW))
                    || (player.getIdx() == 2 && (affectedRow == PLAYER_ONE_FRONT_ROW
                    || affectedRow == PLAYER_ONE_BACK_ROW))) {
                throw new Exception("Selected row does not belong to the current player.");
            }
        }

        if (player.getHero().isLordRoyce()) {
            // freeze all the cards from the affected row
            for (int j = 0; j < TABLE_COLS; j++) {
                if (this.getMinionFromTable(affectedRow, j) != null) {
                    this.getMinionFromTable(affectedRow, j).setFrozen(true);
                }
            }
        }

        if (player.getHero().isEmpressThorina()) {
            destroyCardWithMaxHealthFromRow(affectedRow);
        }

        if (player.getHero().isKingMudface()) {
            for (int j = 0; j < TABLE_COLS; j++) {
                if (this.getMinionFromTable(affectedRow, j) != null) {
                    this.getMinionFromTable(affectedRow, j).increaseHealth(1);
                }
            }
        }

        if (player.getHero().isGeneralKocioraw()) {
            for (int j = 0; j < TABLE_COLS; j++) {
                if (this.getMinionFromTable(affectedRow, j) != null) {
                    Minion minion = this.getMinionFromTable(affectedRow, j);
                    int newAttackDamage = minion.getAttackDamage() + 1;
                    this.getMinionFromTable(affectedRow, j).setAttackDamage(newAttackDamage);
                }
            }
        }

        int newMana = player.getMana() - player.getHero().getCardInfo().getMana();
        player.setMana(newMana);

        // mark hero as "has attacked" this round
        player.setHeroHasAttacked(true);
    }

    /**
     * @return the current round of the game
     */
    public int getCurrRound() {
        return currRound;
    }
}
