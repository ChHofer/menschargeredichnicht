package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by chris on 21.06.2016.
 */
public class Settings extends Activity {

    TextView usernamePref;
    SharedPreferences sharedPref;
    Context context;
    AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        context = getApplicationContext();

        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        usernamePref = (TextView) findViewById(R.id.userTextView);
        usernamePref.setText("Username: "+sharedPref.getString("username",""));

        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
        builder.setMessage("Set your name:")
                .setTitle("Username");

        final EditText input = new EditText(Settings.this);
        input.append(sharedPref.getString("username",""));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Ok",
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                sharedPref.edit().putString("username",input.getText().toString()).commit();
                usernamePref.setText("Username: "+sharedPref.getString("username",""));
                dialog.cancel();
            }
        });
        // Setting Negative "NO" Button
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });

        dialog = builder.create();


        usernamePref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }

}
