package at.se2.gruppe3.menschrgeredichnicht;

/**
 * Created by Oliver on 23.05.2016.
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Client extends Activity {

    private Socket socketClient;

    private static final int SERVERPORT = 5000;
    private static final String SERVER_IP = "10.0.2.2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);

        new Thread(new ClientThread()).start();
    }

    public void onClick(View view) {
        try {

            switch(view.getId()){

                //LAST CHANGES, switch case eingefügt.
                //https://examples.javacodegeeks.com/android/core/socket-core/android-socket-example/

                case R.id.myButton:

                    EditText et = (EditText) findViewById(R.id.EditText01);
                    String str = et.getText().toString();
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socketClient.getOutputStream())),
                            true);
                    out.println(str);

                    break;
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socketClient = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
}