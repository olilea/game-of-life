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
    private static final int MAXIMUM_TICKS_PERS_SECOND = 240;

    private int ticksPerSecond = INITIAL_TICKS_PER_SECOND;
    private long optimalTime = 1000 / ticksPerSecond;

    private final Object lock;

    private final AbstractBoard board;

    private volatile boolean running;

    private JLabel ticksPerSecondLabel;
    private JSpinner tickSpinner;
    private JLabel currentTickLabel;
    private JButton pauseButton;
    private JButton stepButton;
    private JLabel ticksPerRenderLabel;
    private JSpinner renderSpinner;

    public GameOfLifePanel(Dimension d) {
        this.board = (d.height * d.width) < 200_000 ? new Board(d) : new ParallelBoard(d);
        this.lock = new Object();
        initComponents();
        layoutComponents();
        new Thread(this::run).start();
    }

    private void initComponents() {
        ticksPerSecondLabel = new JLabel("Generations per second:");
        tickSpinner = new JSpinner(new SpinnerNumberModel(
                INITIAL_TICKS_PER_SECOND,
                MINIMUM_TICKS_PER_SECOND,
                MAXIMUM_TICKS_PERS_SECOND,
                1
        ));
        tickSpinner.addChangeListener(ae -> setTicksPerSecond(((Integer) tickSpinner.getValue())));
        currentTickLabel = new JLabel(INITIAL_TICKS_PER_SECOND + "");
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(ae -> {
            running = !running;
            pauseButton.setText(running ? "Pause" : "Resume");
            stepButton.setEnabled(!running);
            if (running) {
                synchronized (lock) {
                    lock.notify();
                }
            } else {
                currentTickLabel.setText("--");
            }
        });
        stepButton = new JButton("Step");
        stepButton.setEnabled(false);
        stepButton.addActionListener((ae) -> {
            board.tick();
            board.repaint();
        });
        ticksPerRenderLabel = new JLabel("Generations per render:");
        renderSpinner = new JSpinner(new SpinnerNumberModel(
                1,
                1,
                50,
                1
        ));

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                board.resizeTo(GameOfLifePanel.this.getSize());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }

    private void layoutComponents() {
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        super.add(board);
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(ticksPerSecondLabel, FlowLayout.LEFT);
        controlPanel.add(tickSpinner);
        controlPanel.add(currentTickLabel);
        controlPanel.add(pauseButton);
        controlPanel.add(stepButton);
        controlPanel.add(ticksPerRenderLabel);
        controlPanel.add(renderSpinner);
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
        long ticksSinceRender = 0;
        running = true;
        for (;;) {
            while (running) {
                long now = System.currentTimeMillis();
                long updateLength = now - lastTick;
                lastTick = now;
                double delta = updateLength / (double) optimalTime;
                lastFpsTime += updateLength;
                ++fps;
                if (lastFpsTime > 1000) {
                    final int fpsFin = fps;
                    SwingUtilities.invokeLater(() -> currentTickLabel.setText(Integer.toString(fpsFin)));
                    lastFpsTime = 0;
                    fps = 0;
                }
                if (delta >= 1) {
                    try {
                        board.tick();
                        ++ticksSinceRender;
                        if (ticksSinceRender >= ((Integer) renderSpinner.getValue())) {
                            ticksSinceRender = 0;
                            SwingUtilities.invokeAndWait(board::repaint);
                        }
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
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ie) {
                    // Ignore
                }
            }
        }
    }
}
