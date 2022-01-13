package common.src.main;

public class Field {
    private boolean hit;
    private boolean isShip;

    public Field(){
        this.hit = false;
        this.isShip = false;
    }

    public void toggleHit(){
        this.hit = true;
    }
    public boolean getHit(){
        return this.hit;
    }
    public void setShip(){
        this.isShip = true;
    }
    public boolean isShip(){
        return this.isShip;
    }
}
