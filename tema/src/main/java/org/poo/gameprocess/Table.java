package org.poo.gameprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.cards.Minion;
import org.poo.fileio.Coordinates;

public class Table {
    public static final int TABLE_ROWS = 4;
    public static final int TABLE_COLS = 5;
    public static final int PLAYER_ONE_FRONT_ROW = 2;
    public static final int PLAYER_ONE_BACK_ROW = 3;
    public static final int PLAYER_TWO_FRONT_ROW = 1;
    public static final int PLAYER_TWO_BACK_ROW = 0;

    private final Minion[][] table;

    public Table() {
        this.table = new Minion[TABLE_ROWS][TABLE_COLS];
    }

    /**
     * @return the table
     */
    public Minion[][] getTable() {
        return table;
    }

    /**
     * @param x the row of the minion wanted
     * @param y the column of the minion wanted
     * @return the minion card from the specified coordinates
     */
    public Minion getMinionFromTable(final int x, final int y) {
        return this.table[x][y];
    }

    /**
     * mark all cards on table as "has not attacked"
     */
    public void markCardsHasNotAttacked() {
        for (int i = 0; i < TABLE_ROWS; i++) {
            for (int j = 0; j < TABLE_COLS; j++) {
                if (table[i][j] != null) {
                    table[i][j].setHasAttacked(false);
                }
            }
        }
    }

    /**
     * defrost all frozen cards on table of the current player
     * 'i' is initialised to search only on the corresponding rows of the current player
     * @param currentPlayer the player whose cards needs to be defrosted
     */
    public void defrostCards(final int currentPlayer) {
        int i;
        if (currentPlayer == 1) {
            i = PLAYER_ONE_FRONT_ROW;
        } else {
            i = PLAYER_TWO_BACK_ROW;
        }
        int maxI = i + 2;

        for (; i < maxI; i++) {
            for (int j = 0; j < TABLE_COLS; j++) {
                if (table[i][j] != null && table[i][j].isFrozen()) {
                    table[i][j].setFrozen(false);
                }
            }
        }
    }

    /**
     * @param coords the coordinates of the card wanted from the table
     * @return the minion card from the specified coordinates from the table
     */
    public Minion getCardsFromTableWithCoords(final Coordinates coords) {
        return table[coords.getX()][coords.getY()];
    }

    /**
     * @param enemy the player whose rows to look for a tank card
     * @return the first card found if there is at least one of type tank, otherwise return null
     */
    public Minion existsTankOnEnemyRows(final int enemy) {
        if (enemy == 1) {
            // search on the enemy's front row
            for (int j = 0; j < TABLE_COLS; j++) {
                Minion minion = table[PLAYER_ONE_FRONT_ROW][j];
                if (minion != null && minion.isTank()) {
                    return minion;
                }
            }
            return null;
        }
        // enemy = 2 : search on the enemy's front row
        for (int j = 0; j < TABLE_COLS; j++) {
            Minion minion = table[PLAYER_TWO_FRONT_ROW][j];
            if (minion != null && minion.isTank()) {
                return minion;
            }
        }
        return null;
    }

    /**
     * remove a card from the table with the specified coordinates
     * @param coords the coordinates of the card to be removed
     */
    public void removeCardFromTable(final Coordinates coords) {
        for (int j = coords.getY(); j < TABLE_COLS - 1; j++) {
            table[coords.getX()][j] = table[coords.getX()][j + 1];
        }
        // the last element on the row always becomes null
        table[coords.getX()][TABLE_COLS - 1] = null;

        // update all positions
        for (int i = 0; i < TABLE_ROWS; i++) {
            for (int j = 0; j < TABLE_COLS; j++) {
                if (table[i][j] != null) {
                    table[i][j].setCoords(i, j);
                }
            }
        }
    }

    /**
     * remove the card which has the highest health from the specified row of the table
     * if there are multiple cards with the same health, the first one found is selected
     * @param row the row of the table where the card with the highest health will be destroyed
     */
    public void destroyCardWithMaxHealthFromRow(final int row) {
        if (table[row][0] == null) {
            return;
        }
        Coordinates coordsCardWithMaxHealth = table[row][0].getCoords();
        for (int j = 1; j < TABLE_COLS; j++) {
            if (table[row][j] != null) {
                int cardWithMaxHealthY = coordsCardWithMaxHealth.getY();
                if ((table[row][j]).getHealth() > (table[row][cardWithMaxHealthY]).getHealth()) {
                    coordsCardWithMaxHealth.setY(j);
                }
            }
        }
        // destroy card from the found position
        removeCardFromTable(coordsCardWithMaxHealth);
    }

    /**
     * Converts the frozen cards on the table into an array node representation
     * @param objectMapper the parameter used to create the JSON nodes
     * @return an `ArrayNode` containing the `ObjectNode` representations of the frozen cards
     */
    public ArrayNode frozenCardsToArrayNode(final ObjectMapper objectMapper) {
        ArrayNode tableNode = objectMapper.createArrayNode();
        for (int i = 0; i < TABLE_ROWS; i++) {
            for (int j = 0; j < TABLE_COLS; j++) {
                if (table[i][j] != null && table[i][j].isFrozen()) {
                    ObjectNode minionNode = table[i][j].cardTransformToAnObjectNode(objectMapper);
                    tableNode.add(minionNode);
                }
            }
        }
        return tableNode;
    }

    /**
     * Converts the entire table of minions into an array node representation
     * @param objectMapper the parameter used to create the JSON nodes
     * @return an `ArrayNode` containing `ArrayNode` representations of the rows
     * of minions on the table
     */
    public ArrayNode tableTransformToArrayNode(final ObjectMapper objectMapper) {
        ArrayNode tableNode = objectMapper.createArrayNode();
        for (Minion[] minions : table) {
            // create an ArrayNode to represent the row
            ArrayNode rowNode = objectMapper.createArrayNode();

            // iterate through each column in the row
            for (Minion minion : minions) {
                if (minion != null) {
                    ObjectNode minionNode = minion.cardTransformToAnObjectNode(objectMapper);
                    rowNode.add(minionNode);
                }
            }

            // add the rowNode to the tableNode
            tableNode.add(rowNode);
        }
        return tableNode;
    }
}
