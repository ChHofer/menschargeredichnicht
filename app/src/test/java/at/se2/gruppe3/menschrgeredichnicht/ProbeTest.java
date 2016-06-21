package at.se2.gruppe3.menschrgeredichnicht;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Anita on 14.06.2016.
 */
public class ProbeTest extends ActivityUnitTestCase<MenuActivity>{
    private Spieler spieler;
    private Intent mMainIntent;
    private Kegel[] hauptfelder;
    private Kegel[] rotesFeld;
    private BoardActivity board;



    @Before
    public void setUp() throws Exception {
        super.setUp();
        spieler = new Spieler("Michi");
        mMainIntent = new Intent(Intent.ACTION_MAIN);

    }
    public ProbeTest() {
        super(MenuActivity.class);
    }


    @Test
    public void testButtonActivityA () {
        MenuActivity activity = startActivity(mMainIntent, null, null);
        Button button = (Button) activity.findViewById(R.id.btnHelp);
        button.performClick();
        Intent i = getStartedActivityIntent();
        assertNotNull(i);
        assertTrue(isFinishCalled());
    }

    @Test
    public void checkUsername() throws Exception {
        assertEquals("Michi",spieler.getName());
    }
    @Test
    public void testCheckHauptfeld() throws Exception{
       // hauptfelder = new Kegel[40];
        //rotesFeld = new Kegel[0];
        assertFalse(board.isKegelAtHauptfeld());

    }
}
