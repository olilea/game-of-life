package uk.co.oliverlea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Oliver Lea
 */
public abstract class AbstractBoard extends JPanel {

    protected Dimension boardSize;
    protected Dimension blockSize;

    protected boolean[][] tiles;
    protected boolean[][] nextTiles;
    protected boolean[][] visited;

    private final MouseAdapter mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            applyToTileAtPoint(e.getPoint(), b -> !b);
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            applyToTileAtPoint(e.getPoint(), b -> true);
        }
    };

    public AbstractBoard(Dimension d) {
        this.boardSize = d;
        this.blockSize = new Dimension(5, 5);
        Random r = new Random(System.currentTimeMillis());
        this.tiles = createTiles(r::nextBoolean);
        this.nextTiles = createTiles(() -> false);
        this.visited = new boolean[d.height][d.width];
        initGui();
    }

    private void initGui() {
        super.setPreferredSize(new Dimension(
                boardSize.width * blockSize.width,
                boardSize.height * blockSize.width
        ));
        super.addMouseListener(mouseListener);
        super.addMouseMotionListener(mouseListener);
    }

    private boolean[][] createTiles(Supplier<Boolean> withValue) {
        boolean[][] tiles = new boolean[boardSize.height][boardSize.width];
        for (int row = 0; row < boardSize.height; ++row) {
            for (int col = 0; col < boardSize.width; ++col) {
                tiles[row][col] = withValue.get();
            }
        }
        return tiles;
    }

    public void resizeTo(Dimension d) {
        this.setPreferredSize(d);
        blockSize.setSize(d.width / boardSize.width, d.height / boardSize.height);
    }

    protected boolean shouldLive(int row, int column) {
        int neighbours = getNeighbors(row, column);
        if (getTile(row, column)) {
            switch (neighbours) {
                case 1:
                    return false;
                case 2:
                case 3:
                    return true;
                default:
                    return false;
            }
        } else {
            if (neighbours == 3) {
                return true;
            }
        }
        return false;
    }

    private void applyToTileAtPoint(Point p, Function<Boolean, Boolean> func) {
        int row = (p.y / blockSize.height) % boardSize.height;
        int col = (p.x / blockSize.width) % boardSize.width;
        if (row < 0 || col < 0 || row >= boardSize.width || row >= boardSize.height) return;
        tiles[row][col] = func.apply(tiles[row][col]);
        repaint();
    }

    private int getNeighbors(int row, int column) {
        int width = boardSize.width;
        int height= boardSize.height;
        int leftX = ((column-1) + width) % width;
        int rightX = (column+1) % width;
        int topY = ((row-1) + height) % height;
        int bottomY = (row+1) % height;

        int totalNeighbors = 0;

        if (getTile(topY, column))
            ++totalNeighbors;
        if (getTile(topY, rightX))
            ++totalNeighbors;
        if (getTile(row, rightX))
            ++totalNeighbors;
        if (getTile(bottomY, rightX))
            ++totalNeighbors;
        if (getTile(bottomY, column))
            ++totalNeighbors;
        if (getTile(bottomY, leftX))
            ++totalNeighbors;
        if (getTile(row, leftX))
            ++totalNeighbors;
        if (getTile(topY, leftX))
            ++totalNeighbors;
        return totalNeighbors;
    }

    private boolean getTile(int row, int col) {
        return tiles[row][col];
    }

    public abstract void tick();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCells(g);
        if ((boardSize.height * boardSize.width) < 150_000) {
            drawGrid(g);
        }
    }

    private void drawCells(Graphics g) {
        for (int row = 0; row < boardSize.height; row++) {
            for (int col = 0; col < boardSize.width; col++) {
                if (tiles[row][col]) {
                    visited[row][col] = true;
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(
                        col * blockSize.width,
                        row * blockSize.height,
                        blockSize.width,
                        blockSize.height
                );
                if (!tiles[row][col] && visited[row][col]) {
                    g.setColor(Color.CYAN);
                    g.fillRect(
                            col * blockSize.width,
                            row * blockSize.height,
                            blockSize.width,
                            blockSize.height
                    );
                }
            }
        }
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.BLACK);
        int width = blockSize.width * boardSize.width;
        int height = blockSize.height * boardSize.height;
        for (int i = 0; i < boardSize.height; ++i) {
            g.drawLine(0, i * blockSize.height, width, i * blockSize.height);
        }
        for (int i = 0; i < boardSize.width; ++i) {
            g.drawLine(i * blockSize.width, 0, i * blockSize.width, height);
        }
    }
}
