package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.Coordinates;

public class Table {
    private Minion[][] table;

    public Table() {
        this.table = new Minion[4][5];
    }

    public Minion[][] getTable() {
        return table;
    }

    public Minion getMinionFromTable(int x, int y) {
        return this.table[x][y];
    }
    public void setMinionOnTable(int x, int y, Minion minion) {
        this.table[x][y] = minion;
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

    public void defrostCards(int currentPlayer) {
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

        // reactualizez pozitiile
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                if (table[i][j] != null) {
                    table[i][j].setCoords(i,j);
                }
            }
        }
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
}
