package at.se2.gruppe3.menschrgeredichnicht;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;

import java.util.ArrayList;
import java.util.List;

public class LobbyHost extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener{

    GoogleApiClient mGoogleApiClient;

    private static int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_ETHERNET };

    private boolean mIsConnected;

    private TextView log;

    SharedPreferences sharedPref;

    private ArrayList<String> clientListID = new ArrayList<String>();

    private ArrayList<Device> clientList = new ArrayList<Device>();
    private ArrayList<String> clientListString = new ArrayList<String>();

    ArrayAdapter<Device> adapter;

    Button startButton;
    ListView clientListView;

    Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_host);

        initialize();
    }

    private void initialize(){
        connection = Connection.getInstance();
        connection.clear();
        log = (TextView) findViewById(R.id.logView);

        clientListView = (ListView) findViewById(R.id.clientListView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, clientListString);

        clientListView.setAdapter(adapter);
        clientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendMessage("testMessage");
            }
        });


        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clientList.size()>0){
                    startGame();
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
    }

    private void startGame(){
        Intent newGameScreen = new Intent(getApplicationContext(),
                BoardActivity.class);
        sendMessage("loadgame");
        connection.setHost(true);
        connection.setDeviceList(clientList);
        disconnect();
        startActivity(newGameScreen);
    }

    private void startAdvertising() {
        clientList.clear();
        if (!isConnectedToNetwork()) {
            append("No Network!");
            return;
        }
        // Advertising with an AppIdentifer lets other devices on the
        // network discover this application and prompt the user to
        // install the application.
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(getPackageName()));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        // The advertising timeout is set to run indefinitely
        // Positive values represent timeout in milliseconds
        long NO_TIMEOUT = 0L;

        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        Nearby.Connections.startAdvertising(mGoogleApiClient, sharedPref.getString("username","User"), appMetadata, NO_TIMEOUT,
                this).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                if (result.getStatus().isSuccess()) {
                    Log.w("lolol","Advertising as:"+Nearby.Connections.getLocalEndpointId(mGoogleApiClient));
                    append("Advertising!");
                } else {
                    int statusCode = result.getStatus().getStatusCode();
                    // Advertising failed - see statusCode for more details
                    append("Advertising failed!:"+statusCode);
                }
            }
        });
    }

    private void append(String str){
        log.append(str+"\n");
    }


    private void sendMessage( String message ) {
        if(!clientListID.isEmpty())
            Nearby.Connections.sendReliableMessage( mGoogleApiClient,
                    clientListID,
                    message.getBytes() );
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startAdvertising();
        append("Connected to Google!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        append("Connection Suspended");
    }

    @Override
    public void onConnectionRequest(final String remoteEndpointId, final String remoteDeviceId,
                                    final String remoteEndpointName, final byte[] payload) {
        append("Connection Request");

        if (clientList.size() >= 3) {
            Nearby.Connections.rejectConnectionRequest(mGoogleApiClient,remoteEndpointId);
        }

            byte[] myPayload = null;
            // Automatically accept all requests
            Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, remoteEndpointId,
                    myPayload, this).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        append("Connected to:" + remoteEndpointName);
                        clientList.add(new Device(remoteEndpointId,remoteDeviceId,remoteEndpointName));
                        refreshList();
                    } else {
                        append("Failed to connect to: " + remoteDeviceId);
                    }
                }
            });
    }


    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {
        append("Message Received:"+new String(bytes));
    }

    @Override
    public void onDisconnected(String s) {
        append("Disconnected:"+s);
        for(int i=0;i<clientList.size();i++){
            if(clientList.get(i).getEndpointId().compareTo(s)==0){
                clientList.remove(i);
            }
        }
        refreshList();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        append("Connection Failed");
    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager connManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        for (int networkType : NETWORK_TYPES) {
            NetworkInfo info = connManager.getNetworkInfo(networkType);
            if (info != null && info.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

    private void refreshList(){
        clientListString.clear();
        clientListID.clear();
        for(int i=0;i<clientList.size();i++){
            clientListString.add(clientList.get(i).getEndpointName());
            clientListID.add(clientList.get(i).getEndpointId());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w("lolol","onResume");
        connect();
    }

    private void connect(){
        clientList.clear();
        refreshList();
        mGoogleApiClient.connect();
    }

    private void disconnect(){
        Log.w("lolol","disconnect");
        Nearby.Connections.stopAdvertising(mGoogleApiClient);
        Nearby.Connections.stopAllEndpoints(mGoogleApiClient);
        clientList.clear();
        refreshList();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onPause() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.w("lolol","onStop");
            disconnect();
        }
        super.onPause();
    }

}