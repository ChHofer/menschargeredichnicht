package at.se2.gruppe3.menschrgeredichnicht;
import java.util.Random;

/**
 * Created by Oliver on 02.05.2016.
 */
public class Wurfel {

    Random rnd;

    public Wurfel(){
        rnd = new Random();
    }

    public int wurfelAction(){
        return rnd.nextInt(6)+1;
    }

}
