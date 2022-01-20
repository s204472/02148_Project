package common.src.main;

// This class represents a field on a board.
public class Field {
    private boolean hit;
    private boolean isShip;

    public Field(){
        this.hit = false;
        this.isShip = false;
    }

    public boolean setHit(){
        hit = true;
        return isShip;
    }
    public boolean getHit(){
        return hit;
    }
    public void setShip(){
        isShip = true;
    }
    public boolean isShip(){
        return isShip;
    }
}
