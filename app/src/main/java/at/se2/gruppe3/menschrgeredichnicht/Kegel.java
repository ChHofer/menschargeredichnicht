package at.se2.gruppe3.menschrgeredichnicht;

/**
 * Created by chris on 21.04.2016.
 */
public class Kegel {

    private int State; // 0=StartFelder 1=HauptFelder 2=ZielFelder
    private int Position;
    private int Player;

    public Kegel(int Player,int State,int Position){
        this.Player = Player;
        this.State = State;
        this.Position = Position;
    }

    public int getPlayer() {return Player; }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }

    public int getPosition() {
        return Position;
    }

    public void setPosition(int position) {
        Position = position;
    }
}
