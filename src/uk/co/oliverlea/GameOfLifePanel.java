package uk.co.oliverlea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Oliver Lea
 */
public class GameOfLifePanel extends JPanel {

    private static final int INITIAL_TICKS_PER_SECOND = 5;
    private static final int MINIMUM_TICKS_PER_SECOND = 1;
    private static final int MAXIMUM_TICKS_PERS_SECOND = 60;

    private int ticksPerSecond = INITIAL_TICKS_PER_SECOND;
    private long optimalTime = 1000 / ticksPerSecond;

    private final Board board;

    private JSpinner tickSpinner;
    private JButton pauseButton;

    private volatile boolean running;

    public GameOfLifePanel(Dimension d) {
        this.board = new Board(d);
        initComponents();
        layoutComponents();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                board.resizeTo(GameOfLifePanel.this.getSize());
            }
            @Override public void componentMoved(ComponentEvent e) {}
            @Override public void componentShown(ComponentEvent e) {}
            @Override public void componentHidden(ComponentEvent e) {}
        });
    }

    private void initComponents() {
        tickSpinner = new JSpinner(new SpinnerNumberModel(
                INITIAL_TICKS_PER_SECOND,
                MINIMUM_TICKS_PER_SECOND,
                MAXIMUM_TICKS_PERS_SECOND,
                1
        ));
        tickSpinner.addChangeListener(ae -> setTicksPerSecond(((Integer) tickSpinner.getValue())));
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(ae -> running = !running);
    }

    private void layoutComponents() {
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        super.add(board);
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(tickSpinner, FlowLayout.LEFT);
        controlPanel.add(pauseButton);
        super.add(controlPanel);
        super.setMinimumSize(board.getPreferredSize());
    }

    private void setTicksPerSecond(Integer newValue) {
        if (ticksPerSecond == newValue) return;
        this.ticksPerSecond = newValue;
        this.optimalTime = 1000 / ticksPerSecond;
    }

    public void run() {
        int fps = 0;
        int lastFpsTime = 0;
        long lastTick = System.currentTimeMillis();
        running = true;
        for (;;) {
            if (running) {
                long now = System.currentTimeMillis();
                long updateLength = now - lastTick;
                lastTick = now;
                double delta = updateLength / (double) optimalTime;
                lastFpsTime += updateLength;
                ++fps;
                if (lastFpsTime > 1000) {
                    System.out.println("FPS: " + fps);
                    lastFpsTime = 0;
                    fps = 0;
                }
                if (delta >= 1) {
                    try {
                        SwingUtilities.invokeAndWait(() -> {
                            board.tick();
                            board.repaint();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    long sleepTime = lastTick - System.currentTimeMillis() + optimalTime;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }
}
