package uk.co.oliverlea;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Oliver Lea
 */
public class Tile {

    private boolean alive;

    public Tile(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void invert() {
        this.alive = !this.alive;
    }
}
