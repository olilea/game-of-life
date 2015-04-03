package uk.co.oliverlea;

import javax.swing.*;
import java.awt.*;

/**
 * @author Oliver Lea
 */
public class GameOfLife {

    private GameOfLifePanel panel;

    public GameOfLife(Dimension d) {
        this.panel = new GameOfLifePanel(d);
        show();
        panel.run();
    }

    private void show() {
        JFrame gameFrame = new JFrame("Game of Life");
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.add(this.panel);
        gameFrame.pack();
        gameFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new GameOfLife(new Dimension(800, 500));
    }
}
