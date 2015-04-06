package uk.co.oliverlea;

import java.awt.*;
import java.util.List;

/**
 * @author Oliver Lea
 */
public class Board extends AbstractBoard {

    public Board(Dimension d) {
        super(d);
    }

    @Override
    public void tick() {
        for (int row = 0; row < boardSize.height; row++) {
            for (int col = 0; col < boardSize.width; col++) {
                nextTiles[row][col] = shouldLive(row, col);
            }
        }
        boolean[][] temp = tiles;
        tiles = nextTiles;
        nextTiles = temp;
    }
}
