package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class BoardActivity extends Activity implements BoardView.OnFeldClickedListener{

    private BoardView BView;
    private RelativeLayout TableView;

    private Kegel[][] StartFelder;
    private Kegel[][] ZielFelder;

    private Kegel[] HauptFelder;

    Kegel KegelHighlighted;


    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);





        Intent intent = new Intent(BoardActivity.this, ShakeService.class);
        startService(intent);

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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



    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
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
}