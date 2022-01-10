package common.src.main;

public class Field extends GameBoard{
    private boolean hit;

    public Field(){

    }
    public void toggleHit(){
        hit = true;
    }
    public boolean getHit(){
        return hit;
    }


}
