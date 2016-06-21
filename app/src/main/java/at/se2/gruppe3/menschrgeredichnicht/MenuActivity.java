package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by oliver on 15.04.16.
 */
public class MenuActivity extends Activity implements View.OnClickListener{


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    Button btnNewGame, btnJoinGame, btnHelp, btnSettings;
    Spieler spieler;
    String textname;
    Intent Lobby;
    Context context;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        initialize();

        context = getApplicationContext();

        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Log.w("lolol","sharedPref name:"+sharedPref.getString("username",""));
        if(sharedPref.getString("username","").compareTo("")==0){
            askForName();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }



    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnNewGame:
                Lobby = new Intent(getApplicationContext(),LobbyHost.class);
                startActivity(Lobby);
                break;
            case R.id.btnJoinGame:
                Lobby = new Intent(getApplicationContext(),LobbyClient.class);
                startActivity(Lobby);
                break;
            case R.id.btnHelp:
                Log.w("lolol","Help Clicked!!!");
                Intent newHilfeScreen = new Intent(getApplicationContext(),
                        Hilfe.class);
                startActivity(newHilfeScreen);
                break;
            case R.id.btnSettings:
                Log.w("lolol","Settings Clicked!!!");
                Intent newSettingsScreen = new Intent(getApplicationContext(),
                        Settings.class);
                startActivity(newSettingsScreen);
                break;
        }
    }

    private void askForName(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = new AlertDialog.Builder(MenuActivity.this).create();
        alertDialog.setTitle("Username");
        alertDialog.setMessage("Gib deinen Namen ein:");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        alertDialog.setView(input);

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        textname = input.getText().toString();

                        sharedPref.edit().putString("username",textname).commit();

                        //spieler= new Spieler(textname);
                        //Toast.makeText(getApplicationContext(), spieler.getName() + " ist beigetreten", Toast.LENGTH_LONG).show();
                        //Intent newGameScreen = new Intent(getApplicationContext(),BoardActivity.class);
                        //startActivity(newGameScreen);
                    }
                });

        if(!alertDialog.isShowing()){
            alertDialog.show();
        }
    }

    public void initialize(){
        btnNewGame = (Button) findViewById(R.id.btnNewGame);
        btnNewGame.setOnClickListener(this);
        btnJoinGame = (Button) findViewById(R.id.btnJoinGame);
        btnJoinGame.setOnClickListener(this);
        btnHelp = (Button) findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(this);
        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(this);

    }



    /**
     * SHAKE LISTENER TESTS FROM HERE
     * not working very well :D
     * still too sensitive
     */

    /**TODO
     * improve Shake Listener
     */


}
