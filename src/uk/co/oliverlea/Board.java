package uk.co.oliverlea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * @author Oliver Lea
 */
public class Board extends JPanel {

    private int blockWidth = 5;
    private int blockHeight = 5;

    private Dimension boardSize;

    private List<List<Tile>> tiles;
    private List<List<Tile>> nextTiles;

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private ExecutorService executorService;
    private List<Callable<Void>> parallelTickers;

    public Board(Dimension d) {
        this.boardSize = d;
        super.setPreferredSize(new Dimension(d.width * blockWidth, d.height * blockHeight));
        Random r = new Random(System.currentTimeMillis());
        this.tiles = createTiles(r::nextBoolean);
        this.nextTiles = createTiles(() -> false);
        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                applyToTileAtPoint(e.getPoint(), Tile::invert);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                applyToTileAtPoint(e.getPoint(), t -> t.setAlive(true));
            }
        };
        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseListener);
        this.executorService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
        this.parallelTickers = new ArrayList<>(AVAILABLE_PROCESSORS);
        IntStream.range(0, AVAILABLE_PROCESSORS).forEachOrdered(i -> {
            parallelTickers.add(() -> {
                try {
                    int fromHeight = (boardSize.height / AVAILABLE_PROCESSORS) * i;
                    int toHeight = (boardSize.height / AVAILABLE_PROCESSORS) * (i + 1);
                    for (int row = fromHeight; row < toHeight; row++) {
                        for (int column = 0; column < boardSize.width; column++) {
                            Tile t = tiles.get(row).get(column);
                            boolean nextState = shouldLive(t, row, column);
                            nextTiles.get(row).get(column).setAlive(nextState);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        });
    }

    private List<List<Tile>> createTiles(Supplier<Boolean> withValue) {
        List<List<Tile>> tiles = new ArrayList<>(boardSize.height);
        for (int i = 0; i < boardSize.height; ++i) {
            List<Tile> ts = new ArrayList<>(boardSize.width);
            for (int j = 0; j < boardSize.width; ++j) {
                Tile t = new Tile(withValue.get());
                ts.add(t);
            }
            tiles.add(ts);
        }
        return tiles;
    }

    protected void tick() {
        try {
            List<Future<Void>> fs = executorService.invokeAll(parallelTickers);
            for (Future<Void> f : fs) {
                f.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        List<List<Tile>> temp = tiles;
        tiles = nextTiles;
        nextTiles = temp;
    }

    @Override
    protected void paintComponent(Graphics g) {
        for (int row = 0; row < boardSize.height; row++) {
            for (int col = 0; col < boardSize.width; col++) {
                g.setColor(tiles.get(row).get(col).isAlive() ? Color.BLACK : Color.WHITE);
                g.fillRect(col * blockWidth, row * blockHeight, blockWidth, blockHeight);
            }
        }
    }

    protected void resizeTo(Dimension d) {
        this.setPreferredSize(d);
        blockWidth = d.width / boardSize.width;
        blockHeight = d.height / boardSize.height;
    }

    private boolean shouldLive(Tile t, int row, int column) {
        int neighbours = getNeighbors(row, column);
        if (t.isAlive()) {
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

    private void applyToTileAtPoint(Point p, Consumer<Tile> func) {
        int row = (p.y / blockHeight) % boardSize.height;
        int col = (p.x / blockWidth) % boardSize.width;
        if (row < 0 || col < 0 || row >= boardSize.width || row >= boardSize.height) return;
        func.accept(getTile(row, col));
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

        if (getTile(topY, column).isAlive())
            ++totalNeighbors;
        if (getTile(topY, rightX).isAlive())
            ++totalNeighbors;
        if (getTile(row, rightX).isAlive())
            ++totalNeighbors;
        if (getTile(bottomY, rightX).isAlive())
            ++totalNeighbors;
        if (getTile(bottomY, column).isAlive())
            ++totalNeighbors;
        if (getTile(bottomY, leftX).isAlive())
            ++totalNeighbors;
        if (getTile(row, leftX).isAlive())
            ++totalNeighbors;
        if (getTile(topY, leftX).isAlive())
            ++totalNeighbors;
        return totalNeighbors;
    }

    private Tile getTile(int row, int col) {
        return tiles.get(row).get(col);
    }
}
