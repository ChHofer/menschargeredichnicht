package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Connect extends Activity implements View.OnClickListener {

    Button btnFind, btnStartServer, btnSend;
    TextView textOut;


    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    Client mClient;

    IntentFilter mIntentFilter;

    private List peers = new ArrayList();

    public static final String TAG = "WiFi";

    WifiP2pDevice device;
    WifiP2pConfig config;
    WifiP2pDeviceList peerList;

    private WifiP2pDevice targetDevice;

    private ArrayList<InetAddress> clients = new ArrayList<InetAddress>();


    public WifiP2pInfo p2pInfo;

    InetSocketAddress GOaddress = null;

    private String SERVER_IP;

    public Socket socketClient;



    Thread serverThread = null;
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    public static final int SERVERPORT = 6000;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);
        initialize();

        //Connect.this.startService(new Intent(Connect.this, MyService.class));

        //startService(new Intent(this, MyService.class));


        updateConversationHandler = new Handler();




        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Connect Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://at.se2.gruppe3.menschrgeredichnicht/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        //mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);

    }


    public void initialize() {
        btnFind = (Button) findViewById(R.id.btn_find);
        btnFind.setOnClickListener(this);
        btnStartServer = (Button) findViewById(R.id.btn_startServer);
        btnStartServer.setOnClickListener(this);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);
        //textOut = (TextView) findViewById(R.id.textView2);
    }


    @Override
    public void onClick(View view) {


        switch (view.getId()) {
            case R.id.btn_find:

                testConnection();
                testList();
                break;

            case R.id.btn_startServer:


                this.serverThread = new Thread(new ServerThread());
                this.serverThread.start();



                break;
            case R.id.btn_send:

                try{

                    if(socketClient==null){
                        serverThread.sleep(1500);
                        Log.d(TAG,"SocketClient=null, initiating new SocketClient");
                        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {



                            @Override
                            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                if(p2pInfo.groupFormed){
                                    if (!p2pInfo.isGroupOwner) {
                                        // Joined group as client - connect to GO

                                        startClientService(new InetSocketAddress(p2pInfo.groupOwnerAddress, SERVERPORT));
                            }
                        }
                    }});}
                        else{
                        String str = "This is a test Message from the Client";
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socketClient.getOutputStream())),
                                true);
                        out.println(str);
                    }


                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;


        }
    }

    public void testConnection() {

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

               // Toast.makeText(getApplicationContext(), "Peer Discovery ready", Toast.LENGTH_SHORT).show();
                Log.d("WiFiDBC","Peer Discovery ready");

            }

            @Override
            public void onFailure(int reasonCode) {

              //  Toast.makeText(getApplicationContext(), "Peer Discovery not ready", Toast.LENGTH_SHORT).show();

                Log.d("WiFiDBC","Peer Discovery not ready");
            }

        });
    }


    public void testList(){

        mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {

                displayPeers(peers);


                if(peers.getDeviceList().isEmpty()){
                    Log.d(TAG,"No devices found");
                    Toast.makeText(getApplicationContext(), "No devices found", Toast.LENGTH_SHORT).show();
                }else{
                    //textOut.append(peers.getDeviceList().toString());
                    for (WifiP2pDevice device : peers.getDeviceList())
                    {
                        // device.deviceName
                        Log.d("DEVICE: ", device.deviceName);
                    }
                }





            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Connect Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://at.se2.gruppe3.menschrgeredichnicht/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public void displayPeers(final WifiP2pDeviceList peers)
    {
        //Dialog to show errors/status
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("WiFi Direct File Transfer");

        //Get list view
        ListView peerView = (ListView) findViewById(R.id.peers_listview);

        //Make array list
        ArrayList<String> peersStringArrayList = new ArrayList<String>();

        //Fill array list with strings of peer names
        for(WifiP2pDevice wd : peers.getDeviceList())
        {
            peersStringArrayList.add(wd.deviceName);
        }

        //Set list view as clickable
        peerView.setClickable(true);

        //Make adapter to connect peer data to list view
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, peersStringArrayList.toArray());

        //Show peer data in listview
        peerView.setAdapter(arrayAdapter);


        peerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, int arg2,long arg3) {

                //Get string from textview
                TextView tv = (TextView) view;

                WifiP2pDevice device = null;

                //Search all known peers for matching name
                for(WifiP2pDevice wd : peers.getDeviceList())
                {
                    if(wd.deviceName.equals(tv.getText()))
                        device = wd;
                }

                if(device != null)
                {
                    //Connect to selected peer
                    connectToPeer(device);

                }
                else
                {
                    dialog.setMessage("Failed");
                    dialog.show();

                }
            }
            // TODO Auto-generated method stub
        });

    }

    public void connectToPeer(final WifiP2pDevice wifiPeer)
    {
        this.targetDevice = wifiPeer;

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiPeer.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener()  {
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Connection succeeded", Toast.LENGTH_SHORT).show();
                //setClientStatus("Connection to " + targetDevice.deviceName + " sucessful");

                /*
                Intent ClientScreen = new Intent(getApplicationContext(),
                        Client.class);
                startActivity(ClientScreen);
                */

            }

            public void onFailure(int reason) {
                //setClientStatus("Connection to " + targetDevice.deviceName + " failed");
                Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
            }
        });

    }




/*
    public void startServer() throws IOException {
        clients.clear();
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);;

        // Collect client ip's
        while(true) {
            Socket clientSocket = serverSocket.accept();
            clients.add(clientSocket.getInetAddress());
            clientSocket.close();
        }
    }
*/




    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
                Log.d(TAG,"ServerSocket started");
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                    updateConversationHandler.post(new updateUIThread(read));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            Log.d(TAG,"Client says:"+msg);
            Toast.makeText(getApplicationContext(), "Message from Client: "+msg, Toast.LENGTH_SHORT).show();
            //text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
        }
    }

    public void startClientService(InetSocketAddress address){
        this.GOaddress = address;

        new Thread(new ClientThread()).start();

    }

    //CLIENT THREAD FROM HERE:




    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = GOaddress.getAddress();

                socketClient = new Socket(serverAddr, SERVERPORT);

                Log.d(TAG,"Socket connected?"+socketClient.isConnected());



            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }

}


