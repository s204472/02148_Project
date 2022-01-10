package common.src.main;

public class Ship extends GameObjects {
    private boolean shipHit = false;
    private static int shipsLeft;
    private static int length;
    public Ship(int length) {
        Ship.length = length;
    }

    public void setShipHit(){
        shipHit = true;
    }
    public boolean getShipHit(){
        return shipHit;
    }

}
