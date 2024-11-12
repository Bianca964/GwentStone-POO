package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.Coordinates;

import java.util.Collections;
import java.util.Random;

public class Game {
    private Player playerOne;
    private Player playerTwo;
    private int currentPlayer; // 1 pentru playerOne, 2 pentru playerTwo
    private Minion[][] table;
    private int currRound;

    public Game(Player playerOne, Player playerTwo, int shuffleSeed) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.currentPlayer = playerOne.getTurn() ? 1 : 2;
        this.table = new Minion[4][5];
        this.currRound = 0;

        Collections.shuffle(playerOne.getDeck(), new Random(shuffleSeed));
        Collections.shuffle(playerTwo.getDeck(), new Random(shuffleSeed));
    }

    public int getCurrentPlayer() {
        return currentPlayer == 1 ? 1 : 2;
    }

    public void switchTurn() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        playerOne.setTurn(!playerOne.getTurn());
        playerTwo.setTurn(!playerTwo.getTurn());
    }

    public Player getPlayerOne() {
        return this.playerOne;
    }
    public Player getPlayerTwo() {
        return this.playerTwo;
    }

    public void startNewRound() {
        this.currRound++;
        playerOne.putCardFromDeckInHand();
        playerTwo.putCardFromDeckInHand();
        playerOne.setEndedTurn(false);
        playerTwo.setEndedTurn(false);

        // tb sa marhez cartile de pe masa ca "has not attacked"
        this.markCardsHasNotAttacked();

        // marchez eroii ca "has not attacked"
        playerOne.setHeroHasAttacked(false);
        playerTwo.setHeroHasAttacked(false);
    }

    public void markCardsHasNotAttacked() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                if (table[i][j] != null) {
                    table[i][j].setHasAttacked(false);
                }
            }
        }
    }

    public void defrostCards() {
        int i;
        if (currentPlayer == 1) {
            i = 2;
        } else {
            i = 0;
        }
        int maxI = i + 2;

        // caut cartile frozen de pe randurile jucatorului curent
        for (;i < maxI; i++) {
            for (int j = 0; j < 5; j++) {
                if (table[i][j] != null && table[i][j].isFrozen()) {
                    table[i][j].setFrozen(false);
                }
            }
        }
    }

    public void placeCardOnTable(int handIdx) throws Exception {
        // verific al cui e randul ca sa stiu cu ce jucator lucrez
        if (playerOne.getTurn()) {
            playerOne.placeCardFromHandOnTable(handIdx, this.table, 2, 3);
        } else {
            playerTwo.placeCardFromHandOnTable(handIdx, this.table, 1, 0);
        }
    }

    public Minion getCardsFromTableWithCoords(Coordinates coords) {
        return this.table[coords.getX()][coords.getY()];
    }

    public Minion existsTankOnEnemyRows(int enemy) {
        if (enemy == 1) {
            // caut pe randul 2 al tablei
            for (int j = 0; j < 5; j++) {
                if (table[2][j] != null) {
                    if (table[2][j].getCardInfo().getName().equals("Goliath") || table[2][j].getCardInfo().getName().equals("Warden")) {
                        return table[2][j];
                    }
                }
            }
            return null;
        }
        // enemy = 2 : caut pe randul 1 al tablei
        for (int j = 0; j < 5; j++) {
            if (table[1][j] != null) {
                if (table[1][j].getCardInfo().getName().equals("Goliath") || table[1][j].getCardInfo().getName().equals("Warden")) {
                    return table[1][j];
                }
            }
        }
        return null;
    }

    public void removeCardFromTable(Coordinates coords) {
        for (int j = coords.getY(); j < 4; j++) {
            this.table[coords.getX()][j] = this.table[coords.getX()][j + 1];
        }
        // mereu ultimul element de pe rand va deveni null
        this.table[coords.getX()][4] = null;

        // reactualizez coordonatele
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                if (table[i][j] != null) {
                    table[i][j].setCoords(i, j);
                }
            }
        }

    }

    public void attackCard(Coordinates coordsCardAttacker, Coordinates coordsCardAttacked) throws Exception {
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

        //VERIFIC DACA EXISTA O CARTE TANK PE RANDURILE ADVERSARULUI
        Minion tank = this.existsTankOnEnemyRows(attackedIdx);
        if (tank != null) {
            // VERIFIC DACA CARTEA ATACATA ESTE UNA DIN TANK URI, NU NEAPARAT PRIMUL TANK PE CARE L GASESTE
            if (!cardAttacked.getCardInfo().getName().equals("Warden") && !cardAttacked.getCardInfo().getName().equals("Goliath")) {
                throw new Exception("Attacked card is not of type 'Tank'.");
            }
        }

        // ataca cartea respectiva
        cardAttacked.decreaseHealth(cardAttacker.getAttackDamage());
        // tb sa si scot cartea de pe masa daca isi pierde toata viata dupa atac
        if (cardAttacked.getHealth() <= 0) {
            this.removeCardFromTable(cardAttacked.getCoords());
        }

        cardAttacker.setHasAttacked(true);
    }

    public void cardUsesAbility(Coordinates coordsCardAttacker, Coordinates coordsCardAttacked) throws Exception {
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
            // maresc viata cartii atacate cu 2 (a aceluiasi jucator care si ataca)
            cardAttacked.increaseHealth(2);
            // setez ca a atacat runda asta
            cardAttacker.setHasAttacked(true);
            return;
        }

        // daca aj aici, cartea are una din celelalte 3 abilitati

        // verific daca cartea atacata apartine enemy ului
        if (attackerIdx == attackedIdx) {
            throw new Exception("Attacked card does not belong to the enemy.");
        }

        //VERIFIC DACA EXISTA O CARTE TANK PE RANDURILE ADVERSARULUI
        Minion tank = this.existsTankOnEnemyRows(attackedIdx);
        if (tank != null) {
            // VERIFIC DACA CARTEA ATACATA ESTE UNA DIN TANK URI, NU NEAPARAT PRIMUL TANK PE CARE L GASESTE
            if (!cardAttacked.getCardInfo().getName().equals("Warden") && !cardAttacked.getCardInfo().getName().equals("Goliath")) {
                throw new Exception("Attacked card is not of type 'Tank'.");
            }
        }

        // ataca cartea respectiva
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

            // tb sa si scot cartea de pe masa daca isi pierde toata viata dupa atac
            if (cardAttacked.getHealth() <= 0) {
                this.removeCardFromTable(cardAttacked.getCoords());
            }
        }

        cardAttacker.setHasAttacked(true);
    }

    public void attackHero(Coordinates coordsCardAttacker) throws Exception {
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
        //VERIFIC DACA EXISTA O CARTE TANK PE RANDURILE ADVERSARULUI
        Minion tank = this.existsTankOnEnemyRows(attackedIdx);
        if (tank != null) {
            throw new Exception("Attacked card is not of type 'Tank'.");
        }

        // ATAC EROUL ADVERSARULUI
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

    public void destroyCardWithMaxHealthFromRow(int row) {
        if (table[row][0] == null) {
            return;
        }
        Coordinates coordsCardWithMaxHealth = table[row][0].getCoords();
        for (int j = 1; j < 5; j++) {
            if (table[row][j] != null) {
                if ((table[row][j]).getHealth() > (table[row][coordsCardWithMaxHealth.getY()]).getHealth()) {
                    coordsCardWithMaxHealth.setY(j);
                }
            }
        }
        // distrug cartea de la pozitia gasita
        removeCardFromTable(coordsCardWithMaxHealth);
    }

    public void useHeroAbility(int affectedRow, Player player) throws Exception {
        if (player.getMana() < player.getHero().getCardInfo().getMana()) {
            throw new Exception("Not enough mana to use hero's ability.");
        }
        if (player.getHero().hasAttacked()) {
            throw new Exception("Hero has already attacked this turn.");
        }
        if (player.getHero().isLordRoyce() || player.getHero().isEmpressThorina()) {
            if ((player.getIdx() == 1 && (affectedRow == 2 || affectedRow == 3)) || (player.getIdx() == 2 && (affectedRow == 0 || affectedRow == 1))) {
                throw new Exception("Selected row does not belong to the enemy.");
            }
        }
        if (player.getHero().isGeneralKocioraw() || player.getHero().isKingMudface()) {
            if ((player.getIdx() == 1 && (affectedRow == 0 || affectedRow == 1)) || (player.getIdx() == 2 && (affectedRow == 2 || affectedRow == 3))) {
                throw new Exception("Selected row does not belong to the current player.");
            }
        }

        if (player.getHero().isLordRoyce()) {
            // inghet toate cartile de pe affectedRow
            for(int j = 0; j < 5; j++) {
                if (table[affectedRow][j] != null) {
                    table[affectedRow][j].setFrozen(true);
                }
            }
        }

        if (player.getHero().isEmpressThorina()) {
            destroyCardWithMaxHealthFromRow(affectedRow);
        }

        if (player.getHero().isKingMudface()) {
            for (int j = 0; j < 5; j++) {
                if (table[affectedRow][j] != null) {
                    table[affectedRow][j].increaseHealth(1);
                }
            }
        }

        if (player.getHero().isGeneralKocioraw()) {
            for (int j = 0; j < 5; j++) {
                if (table[affectedRow][j] != null) {
                    int newAttackDamage = table[affectedRow][j].getAttackDamage() + 1;
                    table[affectedRow][j].setAttackDamage(newAttackDamage);
                }
            }
        }

        int newMana = player.getMana() - player.getHero().getCardInfo().getMana();
        player.setMana(newMana);

        // marchez eroul ca atacand runda asta
        player.setHeroHasAttacked(true);
    }

    public int getCurrRound() {
        return currRound;
    }

    public ArrayNode tableTransformToArrayNode(ObjectMapper objectMapper) {
        ArrayNode tableNode = objectMapper.createArrayNode();

        for (int i = 0; i < table.length; i++) {
            // Create an ArrayNode to represent the row
            ArrayNode rowNode = objectMapper.createArrayNode();

            // Iterate through each column in the row
            for (int j = 0; j < table[i].length; j++) {
                Minion minion = table[i][j];
                if (minion != null) {
                    // Transform the minion into an ObjectNode using a transformation method
                    ObjectNode minionNode = minion.cardTransformToAnObjectNode(objectMapper);
                    rowNode.add(minionNode);
                }
            }

            // Add the rowNode to the tableNode
            tableNode.add(rowNode);
        }

        return tableNode;
    }

    public ArrayNode frozenCardsToArrayNode(ObjectMapper objectMapper) {
        ArrayNode tableNode = objectMapper.createArrayNode();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                if (this.table[i][j] != null && this.table[i][j].isFrozen()) {
                    ObjectNode minionNode = this.table[i][j].cardTransformToAnObjectNode(objectMapper);
                    tableNode.add(minionNode);
                }
            }
        }
        return tableNode;
    }

    // Metoda care verifică dacă jocul s-a terminat
    public boolean isGameEnded() {
        if (playerOne.getHero().getHealth() <= 0) {
            playerOne.incrementLoss();
            playerTwo.incrementWin();
            return true;
        } else if (playerTwo.getHero().getHealth() <= 0) {
            playerTwo.incrementLoss();
            playerOne.incrementWin();
            return true;
        }
        return false;
    }
}
