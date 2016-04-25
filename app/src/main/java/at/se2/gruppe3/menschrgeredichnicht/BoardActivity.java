package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class BoardActivity extends Activity {

    private BoardView BoardView;
    private RelativeLayout TableView;

    private Kegel[][] StartFelder;
    private Kegel[][] ZielFelder;

    private Kegel[] HauptFelder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        StartFelder = new Kegel[4][4];
        ZielFelder = new Kegel[4][4];

        HauptFelder = new Kegel[40];

        TableView = (RelativeLayout)findViewById(R.id.tableView);
        TableView.setBackgroundResource(R.drawable.table);

        BoardView = (BoardView)findViewById(R.id.boardView);
        BoardView.setImageResource(R.drawable.board);

        BoardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                    moveDone();
                return true;
            }
        });
    }

    public void moveDone(){
        BoardView.invalidate();
    }

}