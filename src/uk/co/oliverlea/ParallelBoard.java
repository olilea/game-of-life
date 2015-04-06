package uk.co.oliverlea;

import java.awt.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Oliver Lea
 */
public class ParallelBoard extends AbstractBoard {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private ExecutorService executorService;
    private List<Callable<Void>> parallelTickers;

    public ParallelBoard(Dimension d) {
        super(d);
        this.executorService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
        this.parallelTickers = initParallelTickers(AVAILABLE_PROCESSORS);
    }

    private List<Callable<Void>> initParallelTickers(int split) {
        List<Callable<Void>> callables = IntStream.range(0, split-1)
                .mapToObj(i -> tickerFor(
                        (boardSize.height / split) * i,
                        (boardSize.height / split) * (i + 1)))
                .collect(Collectors.toList());
        callables.add(tickerFor(
                    (boardSize.height / split) * (split - 1),
                    boardSize.height
        ));
        return callables;
    }

    private Callable<Void> tickerFor(int fromRow, int toRow) {
        return () -> {
            for (int row = fromRow; row < toRow; row++) {
                for (int column = 0; column < boardSize.width; column++) {
                    nextTiles[row][column] = shouldLive(row, column);
                }
            }
            return null;
        };
    }

    public void tick() {
        try {
            List<Future<Void>> fs = executorService.invokeAll(parallelTickers);
            for (Future<Void> f : fs) {
                f.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        boolean[][] temp = tiles;
        tiles = nextTiles;
        nextTiles = temp;
    }
}
