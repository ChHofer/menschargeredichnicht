package at.se2.gruppe3.menschrgeredichnicht;

import android.test.InstrumentationTestCase;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Anita on 20.06.2016.
 */
public class ConnectionTest extends InstrumentationTestCase{

    private Connection connection;
    private TextView log;
    ListView clientListView;

    private static Connection _instance;
    private ArrayList<Device> clientList;
    int count=0;

    @Before
    public void setUp() throws Exception {
        connection = Connection.getInstance();
        //_instance.getInstance();
    }

   @Test
   public void testDevices(){
       assertNull(connection.getDeviceList());
   }



}
