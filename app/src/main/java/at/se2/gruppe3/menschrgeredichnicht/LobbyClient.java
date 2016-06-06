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
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import java.util.ArrayList;

public class LobbyClient extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener{

    GoogleApiClient mGoogleApiClient;

    SharedPreferences sharedPref;

    private static int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_ETHERNET };

    private boolean mIsConnected;

    private TextView log;

    private String mRemoteHostEndpoint;
    private ArrayList<Device> hostList = new ArrayList<Device>();
    private ArrayList<String> hostListString = new ArrayList<String>();

    ArrayAdapter<String> adapter;
    ListView hostListView;

    Connection connection;
    String serviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_client);

        initialize();
    }

    private void initialize(){
        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        serviceId = getString(R.string.service_id);
        connection = Connection.getInstance();
        connection.clear();

        hostListView = (ListView) findViewById(R.id.hostListView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, hostListString);
        hostListView.setAdapter(adapter);

        hostListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectToEndpoint(hostList.get(position).getEndpointId());
            }
        });

        log = (TextView) findViewById(R.id.logView);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
    }

    private void append(String str){
        log.append(str+"\n");
    }

    private void startDiscovery() {
        hostList.clear();
        refreshList();
        if (!isConnectedToNetwork()) {
            append("No Network!");
            return;
        }

        // Set an appropriate timeout length in milliseconds
        long DISCOVER_TIMEOUT = 60000L;

        // Discover nearby apps that are advertising with the required service ID.
        Nearby.Connections.startDiscovery(mGoogleApiClient, serviceId, DISCOVER_TIMEOUT, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            append("Discovering!");
                        } else {
                            int statusCode = status.getStatusCode();
                            // Advertising failed - see statusCode for more details
                            append("Discovering failed!:"+statusCode);
                        }
                    }
                });
    }

    private void sendMessage( String message ) {
            Nearby.Connections.sendReliableMessage( mGoogleApiClient,
                    mRemoteHostEndpoint,
                    ( sharedPref.getString("username","User") + " says: " + message ).getBytes() );
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startDiscovery();
        append("Connected to Google!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        append("Connection Suspended");
    }

    @Override
    public void onEndpointFound(String endpointId, String deviceId,
                                final String serviceId, String endpointName) {
        append("Endpoint Found! \nName:"+endpointName);
        if(!endpointName.contains("board")) {
            hostList.add(new Device(endpointId, deviceId, endpointName));
            refreshList();
        }
    }

    public void connectToEndpoint(String endpointId){
        Log.w("lolol","Connecting to:"+endpointId);

        byte[] payload = null;
        Nearby.Connections.sendConnectionRequest( mGoogleApiClient, sharedPref.getString("username","User"),
                endpointId, payload, new Connections.ConnectionResponseCallback() {

                    @Override
                    public void onConnectionResponse(String endpointId, Status status, byte[] bytes) {
                        if( status.isSuccess() ) {
                            append("Connected to: " + endpointId );
                            Nearby.Connections.stopDiscovery(mGoogleApiClient, serviceId);
                            mRemoteHostEndpoint = endpointId;

                            for(Device d : hostList){
                                if(d.getEndpointId().compareTo(endpointId)==0){
                                    hostList.clear();
                                    hostList.add(d);
                                }
                            }

                            sendMessage("hi");

                            mIsConnected = true;
                        } else {
                            append("Connection to " + endpointId + " failed");
                            Log.w("lolol","Connection to " + endpointId + " failed: "+status.getStatusCode() +" message:"+status.getStatusMessage());
                            mIsConnected = false;
                        }
                    }
                }, this );
    }

    @Override
    public void onEndpointLost(String s) {
        for(Device d : hostList){
            if(d.getEndpointId().compareTo(s)==0){
                hostList.remove(d);
                refreshList();
            }
        }
        append("Endpoint lost");
        startDiscovery();
    }

    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {
        String message=new String(bytes);
        append("Message Received:"+message);
        if(message.compareTo("loadgame") ==0){
            startGame();
        }
    }

    private void startGame(){
        Intent newGameScreen = new Intent(getApplicationContext(),
                BoardActivity.class);
        connection.setHost(false);
        connection.setDeviceList(hostList);
        disconnect();
        startActivity(newGameScreen);
    }

    @Override
    public void onDisconnected(String s) {
        append("Disconnected");
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
        hostListString.clear();
        for(int i=0;i<hostList.size();i++){
            hostListString.add(hostList.get(i).getEndpointName());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        connect();
    }

    private void connect(){
        hostList.clear();
        refreshList();
        mGoogleApiClient.connect();
    }

    private void disconnect(){
        Log.w("lolol","disconnect");
        hostList.clear();
        refreshList();
        Nearby.Connections.stopDiscovery(mGoogleApiClient,serviceId);
        Nearby.Connections.stopAllEndpoints(mGoogleApiClient);
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
