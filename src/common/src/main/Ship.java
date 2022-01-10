package common.src.main;

public class Ship extends GameBoard{
    private boolean hit;

    public Ship() {
    }

    public void toggleHit() {
        hit = true;
    }

    public boolean getHit() {
        return hit;
    }
}
