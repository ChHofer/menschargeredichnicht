package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

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

public class BoardActivity extends Activity implements BoardView.OnFeldClickedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.MessageListener,
        Connections.ConnectionRequestListener,
        Connections.EndpointDiscoveryListener{

    private BoardView BView;
    private RelativeLayout TableView;

    private Kegel[][] StartFelder;
    private Kegel[][] ZielFelder;

    private Kegel[] HauptFelder;

    Kegel KegelHighlighted;

    private static int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_ETHERNET };

    Boolean isHost;

    SharedPreferences sharedPref;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private ArrayList<String> deviceListID = new ArrayList<String>();

    private ArrayList<Device> deviceListOld = new ArrayList<Device>();
    private ArrayList<String> deviceListOldId = new ArrayList<String>();

    private String mRemoteHostEndpoint;

    GoogleApiClient mGoogleApiClient;
    Connection connection;

    ProgressDialog progressDialog;
    String serviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        initialize();
    }

    private void initialize(){
        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        serviceId = getString(R.string.service_id);

        // Network
        connection = Connection.getInstance();
        deviceListOld = connection.getDeviceList();
        for(Device d : deviceListOld){
            deviceListOldId.add(d.getDeviceId());
        }

        Log.w("lolol","Size:"+ connection.getDeviceList().size());
        isHost = connection.isHost();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();


        Intent intent = new Intent(BoardActivity.this, ShakeService.class);
        startService(intent);

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            Wurfel wurfel = new Wurfel();

            @Override
            public void onShake(int count) {
                Toast.makeText(getApplicationContext(), "Würfelzahl = "+wurfel.wurfelAction(), Toast.LENGTH_SHORT).show();
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(300);
            }
        });

        StartFelder = new Kegel[4][4];
        ZielFelder = new Kegel[4][4];

        HauptFelder = new Kegel[40];

        TableView = (RelativeLayout)findViewById(R.id.tableView);
        TableView.setBackgroundResource(R.drawable.table);

        BView = (BoardView)findViewById(R.id.boardView);
        BView.setImageResource(R.drawable.board);

        startGame();
        moveDone();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Warte auf Mitspieler...");
        progressDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        connect();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void startGame(){
        for(int i=0;i<=3;i++){
            for(int j=0;j<=3;j++){
                StartFelder[i][j] = new Kegel(i,0,j);
            }
        }
        Log.w("Logger","StartGame");
    }

    public void moveDone(){
        BView.setBoardState(StartFelder,ZielFelder,HauptFelder);
        BView.invalidate();
        Log.w("Logger","MoveDone");
    }

    @Override
    public void OnFeldClicked(int state,int player,int position) {
        sendMessage("Click!");
        switch(state){
            case 0:
                if(StartFelder[player][position]!=null){
                    KegelHighlighted = StartFelder[player][position];
                    BView.highlightKegel(KegelHighlighted);
                }
                break;
            case 1:
                if(KegelHighlighted!=null){
                    moveKegel(KegelHighlighted,state,position);
                }else{
                    KegelHighlighted = HauptFelder[position];
                    BView.highlightKegel(KegelHighlighted);
                }
                break;
            case 2:
                if(KegelHighlighted!=null){
                    if(KegelHighlighted.getPlayer() == player) {
                        moveKegel(KegelHighlighted, state, position);
                    }
                }else{
                    KegelHighlighted = ZielFelder[player][position];
                    BView.highlightKegel(KegelHighlighted);
                }
                break;
        }
        moveDone();
    }

    public void moveKegel(Kegel k,int state, int position){
        removeKegel(k);
        switch(state) {
            case 0:
                StartFelder[k.getPlayer()][position] = new Kegel(k.getPlayer(),state,position);
                break;
            case 1:
                if(HauptFelder[position]!=null){
                    kickKegel(HauptFelder[position]);
                }
                HauptFelder[position] = new Kegel(k.getPlayer(),state,position);
                break;
            case 2:
                if(ZielFelder[k.getPlayer()][position] != null){
                    break;
                }
                ZielFelder[k.getPlayer()][position] = new Kegel(k.getPlayer(),state,position);
                break;
        }
        BView.resetHighlight();
        KegelHighlighted = null;
        moveDone();
    }

    public void kickKegel(Kegel k){
        for(int i=0;i<StartFelder[k.getPlayer()].length;i++){
            if(StartFelder[k.getPlayer()][i]==null){
                StartFelder[k.getPlayer()][i] = new Kegel(k.getPlayer(),0,i);
                break;
            }
        }
    }

    public void removeKegel(Kegel k){
        switch(k.getState()) {
            case 0:
                StartFelder[k.getPlayer()][k.getPosition()] = null;
                break;
            case 1:
                HauptFelder[k.getPosition()] = null;
                break;
            case 2:
                ZielFelder[k.getPlayer()][k.getPosition()] = null;
                break;
        }
    }

    private void sendMessage( String message ) {

        if(isHost){
            Nearby.Connections.sendReliableMessage( mGoogleApiClient,
                    deviceListID,
                    ( Nearby.Connections.getLocalDeviceId( mGoogleApiClient ) + " says: " + message ).getBytes() );
        }else{
            Nearby.Connections.sendReliableMessage( mGoogleApiClient,
                    mRemoteHostEndpoint,
                    ( Nearby.Connections.getLocalDeviceId( mGoogleApiClient ) + " says: " + message ).getBytes() );
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(isHost){
            startAdvertising();
        }else{
            startDiscovery();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onEndpointFound(String endpointId, String deviceId,
                                final String serviceId, String endpointName) {
        Log.w("lolol","Endpoint Found!");
        if(!isHost){
            if(deviceListOld.get(0).getEndpointName().compareTo(endpointName)==0){
                connectToEndpoint(endpointId,deviceId,serviceId);
            }
        }
    }

    public void connectToEndpoint(String endpointId, String deviceId, final String serviceId){

        byte[] payload = null;
        Nearby.Connections.sendConnectionRequest( mGoogleApiClient, deviceId,
                endpointId, payload, new Connections.ConnectionResponseCallback() {

                    @Override
                    public void onConnectionResponse(String endpointId, Status status, byte[] bytes) {
                        if( status.isSuccess() ) {
                            Log.w("lolol","Connected to: " + endpointId );
                            Nearby.Connections.stopDiscovery(mGoogleApiClient, serviceId);
                            mRemoteHostEndpoint = endpointId;

                            progressDialog.dismiss();
                            //sendMessage("hi");

                            //mIsConnected = true;
                        } else {
                            Log.w("lolol","Connection to " + endpointId + " failed" );
                            //mIsConnected = false;
                        }
                    }
                }, this );
    }


    @Override
    public void onEndpointLost(String s) {
    }

    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {
        String message=new String(bytes);
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

        Log.w("lolol","Message Received:"+message);
    }

    @Override
    public void onDisconnected(String s) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionRequest(final String remoteEndpointId, final String remoteDeviceId,
                                    final String remoteEndpointName, byte[] payload) {

        Log.w("lolol", "Connection Request");
        Boolean valid=false;
        for (String string : deviceListOldId) {
            if(string.compareTo(remoteDeviceId)==0){
                valid=true;
            }
        }

        if(valid) {
            Log.w("lolol", "Accept Request");
            byte[] myPayload = null;

            Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, remoteEndpointId,
                    myPayload, this).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.w("lolol", "Connected to:" + remoteEndpointName);
                        deviceList.add(new Device(remoteEndpointId, remoteDeviceId, remoteEndpointName));
                        refreshList();

                        if (deviceListOld.size() == deviceList.size()) {
                            progressDialog.dismiss();
                            sendMessage("gamestart");
                        }
                    } else {
                        Log.w("lolol", "Failed to connect to: " + remoteEndpointName);
                    }
                }
            });
        }else{
            Nearby.Connections.rejectConnectionRequest(mGoogleApiClient, remoteEndpointId);
        }
    }



    // Discovery + Advertising



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

    private void startAdvertising() {

        if (!isConnectedToNetwork()) {
            Log.w("lolol","No Network!");
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
        long NO_TIMEOUT = 60000L;

        Nearby.Connections.startAdvertising(mGoogleApiClient, sharedPref.getString("username","user"), appMetadata, NO_TIMEOUT,
                this).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                if (result.getStatus().isSuccess()) {
                    Log.w("lolol","Advertising!");
                } else {
                    int statusCode = result.getStatus().getStatusCode();
                    // Advertising failed - see statusCode for more details
                    Log.w("lolol","Advertising failed!:"+statusCode);
                }
            }
        });
    }

    private void startDiscovery() {
        //hostList.clear();
        //refreshList();
        if (!isConnectedToNetwork()) {
            Log.w("lolol","No Network!");
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
                            Log.w("lolol","Discovering!");
                        } else {
                            int statusCode = status.getStatusCode();
                            // Advertising failed - see statusCode for more details
                            Log.w("lolol","Discovering failed!:"+statusCode);
                        }
                    }
                });
    }

    private void refreshList(){
        deviceListID.clear();
        for(int i=0;i<deviceList.size();i++){
            deviceListID.add(deviceList.get(i).getEndpointId());
        }
    }


    private void connect(){
        mGoogleApiClient.connect();
    }

    private void disconnect(){
        deviceList.clear();
        refreshList();
        Log.w("lolol","disconnect");
        stopDiscoveryAdvertising();
        Nearby.Connections.stopAllEndpoints(mGoogleApiClient);
        mGoogleApiClient.disconnect();
    }

    public void stopDiscoveryAdvertising(){
        Nearby.Connections.stopDiscovery(mGoogleApiClient,serviceId);
        Nearby.Connections.stopAdvertising(mGoogleApiClient);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            disconnect();
        }
        super.onPause();
    }
}