package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

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

    private int playerActive;

    private static int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_ETHERNET };

    Boolean isHost;
    private int myPosition;
    Boolean isMyTurn = false;
    Boolean mayRollDice = false;

    SharedPreferences sharedPref;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private ArrayList<String> deviceListID = new ArrayList<String>();

    private ArrayList<Device> deviceListOld = new ArrayList<Device>();
    private ArrayList<String> deviceListOldId = new ArrayList<String>();

    TextView[] playerTextViews = new TextView[4];

    private String mRemoteHostEndpoint;

    GoogleApiClient mGoogleApiClient;
    Connection connection;

    ProgressDialog progressDialog;
    String serviceId;
    int counter;
    int rand;
    int Zahl;
    boolean opponentDice;

    private ArrayList<String> userDeviceList = new ArrayList<String>();


    //DICE VARIABLES

    Timer timer = new Timer();

    Random rnd = new Random();
    private ImageView diceimage;


    //END DICE VARIABLES

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

        playerTextViews[0] = (TextView) findViewById(R.id.player0);
        playerTextViews[1] = (TextView) findViewById(R.id.player1);
        playerTextViews[2] = (TextView) findViewById(R.id.player2);
        playerTextViews[3] = (TextView) findViewById(R.id.player3);

        // Network
        connection = Connection.getInstance();
        deviceListOld = connection.getDeviceList();

        for(Device d : deviceListOld){
            deviceListOldId.add(d.getDeviceId());
        }


        Log.w("lolol","Size:"+ connection.getDeviceList().size());
        isHost = connection.isHost();
        Log.d("IS HOST?",isHost.toString());

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
                if(isMyTurn && mayRollDice){
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    Zahl = wurfelAction();
                    sendMessage("$dice#" + Zahl);

                    startDiceAnimation();
                    v.vibrate(300);
                    mayRollDice = false;
                }
            }
        });

        StartFelder = new Kegel[4][4];
        ZielFelder = new Kegel[4][4];

        HauptFelder = new Kegel[40];

        TableView = (RelativeLayout)findViewById(R.id.tableView);
        TableView.setBackgroundResource(R.drawable.table);

        BView = (BoardView)findViewById(R.id.boardView);
        BView.setImageResource(R.drawable.board);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Warte auf Mitspieler...");
        progressDialog.show();

        //DICE INITIALIZATION

        diceimage = (ImageView) findViewById(R.id.dice);
    }

    public int wurfelAction() {

        int zahl = rnd.nextInt(6) + 1;
        return zahl;
    }

    public int wurfelAction(int in){
        return in;

    }

    public void setPicture(int rndZahl) {

        switch (rndZahl) {
            case 1:
                diceimage.setImageResource(R.drawable.one);
                break;
            case 2:
                diceimage.setImageResource(R.drawable.two);
                break;
            case 3:
                diceimage.setImageResource(R.drawable.three);
                break;
            case 4:
                diceimage.setImageResource(R.drawable.four);
                break;
            case 5:
                diceimage.setImageResource(R.drawable.five);
                break;
            case 6:
                diceimage.setImageResource(R.drawable.six);
                break;
        }
    }

    public void startDiceAnimation(){

        Animation startrotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anim);
        startrotateAnimation.setDuration(1000);
        startrotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                diceimage.setImageResource(R.drawable.dice3droll);
            }

            @Override
            public void onAnimationEnd(Animation animation) {


                if(opponentDice){
                    setPicture(rand);
                    opponentDice = false;
                    return;
                }


                if(isMyTurn){
                    setPicture(Zahl);
                    if(Zahl != 6 && isStartFeldFull()){
                        if(isHost){

                            isMyTurn = false;
                            addCounter();
                        }else{
                            isMyTurn = false;
                            sendMessage("$noSixToGoOut");
                        }
                    }
                }




            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        diceimage.startAnimation(startrotateAnimation);
    }

    public boolean isStartFeldFull(){
        for(int i = 0; i < StartFelder[myPosition].length; i++){
            if(StartFelder[myPosition][i] == null) return false;
        }
        return true;
    }




    @Override
    public void onResume() {
        super.onResume();
        connect();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void startGame(){
        int players;
        if(isHost) players = deviceListOld.size()+1;
        else players = userDeviceList.size();

        for(int i=0;i<players;i++){
            for(int j=0;j<=3;j++){
                StartFelder[i][j] = new Kegel(i,0,j);
            }
        }
        setPlayerColor(0);
        Log.w("Logger","StartGame");
    }

    public void moveDone(){
        BView.setBoardState(StartFelder,ZielFelder,HauptFelder);
        BView.invalidate();
        //Log.w("Logger","MoveDone");
    }

    @Override
    public void OnFeldClicked(int state,int player,int position) {

        if(!isMyTurn) {
            return;
        }

        if(KegelHighlighted==null){

            switch(state){
                case 0:
                    if (StartFelder[player][position] != null && StartFelder[player][position].getPlayer() != myPosition) return;
                    if (Zahl != 6) return;
                    break;
                case 1: if(HauptFelder[position]!=null) if(HauptFelder[position].getPlayer()!=myPosition) return;
                    break;
                case 2: if(ZielFelder[player][position]!=null) if(ZielFelder[player][position].getPlayer()!=myPosition) return;
                    break;
            }
        }

        sendMessage("$click#" + state + "," + player + "," + position);

        OnFeldClickedMessage(state,player,position);
    }


    public void OnFeldClickedMessage(int state,int player,int position) {

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

        isMyTurn = false;
        if(isHost){
            addCounter();
        }/*else{
            if(isMyTurn){
                sendMessage("$moveCompleted");
            }

        }*/



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
                    ( sharedPref.getString("username","user") + " says: " + message ).getBytes() );
                    //( Nearby.Connections.getLocalDeviceId( mGoogleApiClient ) + " says: " + message ).getBytes() );
        }else{
            Nearby.Connections.sendReliableMessage( mGoogleApiClient,
                    mRemoteHostEndpoint,
                    ( sharedPref.getString("username","user") + " says: " + message ).getBytes() );
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(isHost){
            startAdvertising();
            myPosition = 0;
            isMyTurn = true;
            mayRollDice = true;
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
            if((deviceListOld.get(0).getEndpointName()+"board").compareTo(endpointName)==0){
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
        //Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

        Log.d("MessageReceived: ",message);

        if(message.contains("gamestart")){
            progressDialog.dismiss();
            stopDiscoveryAdvertising();
        }if(message.contains("$dice")){

            if(isHost){
                sendMessage(message);
            }

            if(!message.contains(sharedPref.getString("username","user"))){
                Toast.makeText(this,message.split("says")[0]+"hat gewürfelt",Toast.LENGTH_SHORT).show();
                message = message.split("#")[1];
                rand = Integer.parseInt(message);
                opponentDice = true;
                startDiceAnimation();
            }



        }

        if(!isHost){
            if(message.contains("$deviceList")){
                message = message.split("#")[1];

                String[] list = message.split(",");

                for(int i = 0; i < list.length; i++){
                    userDeviceList.add(list[i]);
                    playerTextViews[i].setText(list[i]);
                    if(list[i].equals(sharedPref.getString("username","user"))){
                        myPosition = i;
                    }

                    Log.d("userDeviceList=",userDeviceList.toString());
                    Log.d("myPosition=",String.valueOf(myPosition));

                    startGame();
                    moveDone();
                }
            }if(message.contains("$click")){


                message = message.split("#")[1];

                String[] list = message.split(",");

                if(Integer.parseInt(list[1]) != myPosition){
                    OnFeldClickedMessage(Integer.parseInt(list[0]),Integer.parseInt(list[1]),Integer.parseInt(list[2]));
                }




            }if(message.contains("$isOnTurn")){
                message = message.split("#")[1];

                counter = Integer.parseInt(message);

                setPlayerColor(counter);
                if(counter == myPosition){


                    /*
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    */


                    isMyTurn = true;
                    mayRollDice = true;
                }

            }
        }if(isHost){
            /*if(message.contains("$moveCompleted")){
                addCounter();
            }*/
            if(message.contains("$click")){

                sendMessage(message);

                message = message.split("#")[1];

                String[] list = message.split(",");

                OnFeldClickedMessage(Integer.parseInt(list[0]),Integer.parseInt(list[1]),Integer.parseInt(list[2]));

            }if(message.contains("$noSixToGoOut")) {


                try {
                    Thread.sleep(1100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                addCounter();
            }

        }

        Log.d("userDeviceList: ",userDeviceList.toString());

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
                            stopDiscoveryAdvertising();
                            broadCastDeviceList();

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


    private void broadCastDeviceList(){
        String listOut = "$deviceList#";

        listOut += sharedPref.getString("username","user") + ",";


        playerTextViews[0].setText(sharedPref.getString("username","user"));
        for(int i = 0;i<deviceListOld.size();i++){
            playerTextViews[i+1].setText(deviceListOld.get(i).getEndpointName());
            listOut += deviceListOld.get(i).getEndpointName()+",";
        }

        sendMessage(listOut);

        startGame();
        moveDone();

        Log.d("listOutHost: ",listOut);
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

        Nearby.Connections.startAdvertising(mGoogleApiClient, sharedPref.getString("username","user")+"board", appMetadata, NO_TIMEOUT,
                this).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                if (result.getStatus().isSuccess()) {
                    Log.w("lolol","Advertising as:"+Nearby.Connections.getLocalEndpointId(mGoogleApiClient));
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

    private void addCounter(){


        try {
            Thread.sleep(900);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        counter++;
        counter = counter %(deviceListOld.size()+1);

        if(counter == 0){
            isMyTurn = true;
            mayRollDice = true;
        }
        setPlayerColor(counter);

        sendMessage("$isOnTurn#" + counter);

        Log.d("isMyTurn:" , isMyTurn.toString());
        Log.d("Counter:",String.valueOf(counter));
    }

    private void setPlayerColor(int player){

        for(int i=0;i<3;i++){
            if(player==i) playerTextViews[i].setBackgroundColor(Color.parseColor("#8866FF7A"));
            else playerTextViews[i].setBackgroundColor(Color.parseColor("#0066FF7A"));
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