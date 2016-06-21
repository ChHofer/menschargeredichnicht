package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.SingleLaunchActivityTestCase;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by Anita on 14.06.2016.
 */
public class ActivityTest extends ActivityUnitTestCase<MenuActivity>{
    MenuActivity activity;

    public ActivityTest() {
        super(MenuActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();


    }

    @Test
    public void testButtonHelp() {
        // register next activity that need to be monitored.
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(Hilfe.class.getName(), null, false);
        // open current activity.
        final Button btnHilfe = (Button) activity.findViewById(R.id.btnHelp);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // click button and open next activity.
                btnHilfe.performClick();
            }
        });
        Activity nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        // next activity is opened and captured.
        assertNotNull(nextActivity);
        nextActivity .finish();
        assertTrue(isFinishCalled());
    }
    @Test
    public void testButtonNewGame() {
        // register next activity that need to be monitored.
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(LobbyHost.class.getName(), null, false);
        // open current activity.
        final Button btnNGame = (Button) activity.findViewById(R.id.btnNewGame);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // click button and open next activity.
                btnNGame.performClick();
            }
        });
        Activity nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        // next activity is opened and captured.
        assertNotNull(nextActivity);
        nextActivity .finish();
        assertTrue(isFinishCalled());
    }
    @Test
    public void testButtonJoinGame() {
        // register next activity that need to be monitored.
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(LobbyClient.class.getName(), null, false);
        // open current activity.
        final Button btnJGame = (Button) activity.findViewById(R.id.btnJoinGame);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // click button and open next activity.
                btnJGame.performClick();
            }
        });
        Activity nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        // next activity is opened and captured.
        assertNotNull(nextActivity);
        nextActivity .finish();
        assertTrue(isFinishCalled());
    }


    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
